package de.magisit.vncclient.protocol.encodings.frame

import android.graphics.Color
import androidx.core.graphics.set
import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.encodings.FrameEncoding
import de.magisit.vncclient.utils.ExtendedDataInputStream
import java.util.zip.Inflater
import kotlin.math.min


class ZRLEEncoding : FrameEncoding(encodingId = 16) {
    // Indexes and palettes
    var curPalettePosition: Int = 0
    var runComplete: Boolean = false
    var currentByte: Byte = 0
    var runLength: Int = 0
    var packedPixelSize: Int = 0
    var currentTileX: Int = 0
    var currentTileY: Int = 0
    var packedPixelIndex: Int = 0
    var packedPixels: ByteArray = ByteArray(4800)
    var cPixelPalette = IntArray(10)

    // Variables for the color
    var curRed: Int = 0
    var curGreen: Int = 0
    var curBlue: Int = 0
    var currentColor: Int = 0

    // Byte arrays and variables for the zlib data
    var inflater = Inflater()
    var currentPosition: Int = 0
    var zLibDataLength: Int = 0
    var zLibData: ByteArray = ByteArray(1000)
    var inflaterBuffer = ByteArray(150000)
    var maxUncompressedLength: Int = 0

    override fun readAndDecode(
        inputStream: ExtendedDataInputStream,
        rfbClient: RfbClient,
        width: Int,
        height: Int,
        xPosition: Int,
        yPosition: Int
    ) {
        // Reset the current position
        currentPosition = 0

        // Read the zlib data length
        zLibDataLength = inputStream.readInt()

        // If the length is larger than the array, initialize a new array with double the size
        if (zLibDataLength > zLibData.size) zLibData = ByteArray(zLibDataLength * 2)

        // Reda the zlib data from the input stream
        inputStream.readFully(zLibData, 0, zLibDataLength)

        // Set input of inflater
        inflater.setInput(zLibData, 0, zLibDataLength)

        // Calculate the maximum size of the uncompressed data since we have no info about that
        // TODO Not really save to do, since there is no way to calculate the exact maxSize, so maybe think about some kind of streams for the deflater if possible
        maxUncompressedLength = width * height * 3 + ((width % 64 + 1) * (height % 64 + 1))

        // If the buffer size is to small set it to the max uncompressed size
        if (inflaterBuffer.size < maxUncompressedLength)
            inflaterBuffer = ByteArray(maxUncompressedLength)

        // Inflate the zlib data
        inflater.inflate(inflaterBuffer)

        // Define a tileY
        var tileY = yPosition
        // Repeat as long as there are tiles left in the current rectangle column
        while (tileY < yPosition + height) {
            // Set the tile height (a tile is max 64 pixels high or smaller if there are no pixels left in the current tile)
            val tileHeight = min(yPosition + height - tileY, 64)

            // Define a tileX
            var tileX = xPosition
            // Repeat as long as there are tiles left in the current rectangle row
            while (tileX < xPosition + width) {
                // Set the tile width (a tile is max 62 pixels wide or smaller if there are no pixels left in the current tile)
                val tileWidth = min(xPosition + width - tileX, 64)

                // Read the mode (a subencoding type value which includes information about the current (encoding type and palette length if needed)
                val mode = inflaterBuffer[currentPosition++]

                // If the tile is run length encoded the top bit is set, otherwise not
                val isRunLengthEncoded = (mode.toInt() and 128) != 0
                // If a palette is needed it has the length of the last 7 bits
                val paletteSize = mode.toInt() and 127

                // If the palette size is to small to fit the current palette, reinitialize a new array
                if (cPixelPalette.size < paletteSize) cPixelPalette = IntArray(paletteSize)
                // Read the palette
                this.readPalette(paletteSize)

                if (!isRunLengthEncoded) {
                    // Tile is not run length encoded
                    when {
                        paletteSize == 0 -> {
                            // Palette size is zero which means that there is no palette and the server sends raw pixels
                            // Reset the currentTileX and currentTileY variables
                            currentTileX = 0
                            currentTileY = 0

                            // Iterate through each pixel of the tile and set the value to the corresponding color
                            repeat(tileWidth * tileHeight) {

                                // Read the current pixel value
                                curBlue =
                                    0x00 shl 24 or (inflaterBuffer[currentPosition++].toInt() and 0xff)
                                curGreen =
                                    0x00 shl 24 or (inflaterBuffer[currentPosition++].toInt() and 0xff)
                                curRed =
                                    0x00 shl 24 or (inflaterBuffer[currentPosition++].toInt() and 0xff)

                                // Set the pixel
                                rfbClient.bitmap[tileX + currentTileX, tileY + currentTileY] =
                                    Color.rgb(curRed, curGreen, curBlue)

                                // If the row has ended reset the x value to 0 and increase the y value by 1
                                if (currentTileX++ == tileWidth) {
                                    currentTileX = 0
                                    currentTileY++
                                }
                            }
                        }
                        paletteSize == 1 -> {
                            // Palette size is one which means that the tile hsa one color which the server sends

                            // Read the color of the tile
                            curBlue =
                                0x00 shl 24 or (inflaterBuffer[currentPosition++].toInt() and 0xff)
                            curGreen =
                                0x00 shl 24 or (inflaterBuffer[currentPosition++].toInt() and 0xff)
                            curRed =
                                0x00 shl 24 or (inflaterBuffer[currentPosition++].toInt() and 0xff)

                            // Set the color
                            currentColor = Color.rgb(curRed, curGreen, curBlue)

                            // Fill the tile with the color
                            repeat(tileHeight) { y ->
                                repeat(tileWidth) { x ->
                                    rfbClient.bitmap[tileX + x, tileY + y] = currentColor
                                }
                            }
                        }
                        paletteSize <= 16 -> {
                            // Palette size is less equal than 16 which means that the server sends a palette
                            // and encodes the pixels in 1,2 or 4 bit

                            // Reset the packed pixel index
                            packedPixelIndex = 0
                            when {
                                paletteSize == 2 -> {
                                    // The palette size is two which means that the server sends 1 bit per pixel

                                    // Reset the current x and y coordinates
                                    currentTileX = 0
                                    currentTileY = 0

                                    // Calculate the count of total packed pixels
                                    packedPixelSize = ((tileWidth + 7) / 8) * tileHeight

                                    // If the buffer is to small reinitialize it
                                    if (packedPixels.size < packedPixelSize) packedPixels =
                                        ByteArray(packedPixelSize)

                                    // Fill the packed pixel buffer
                                    repeat(packedPixelSize) {
                                        packedPixels[packedPixelIndex++] =
                                            inflaterBuffer[currentPosition++]
                                    }

                                    // Iterate through each packed pixel
                                    repeat(packedPixelSize) { index ->
                                        // Iterate over the needed bits for the current encoding and set the pixel value for the current pixel
                                        for (i in 7 downTo 0) {
                                            rfbClient.bitmap[tileX + currentTileX, tileY + currentTileY] =
                                                cPixelPalette[packedPixels[index].toInt() shr i and 1]

                                            if (tileX + ++currentTileX == tileX + tileWidth) {
                                                currentTileY++
                                                currentTileX = 0
                                                break
                                            }
                                        }
                                    }
                                }
                                paletteSize <= 4 -> {
                                    // The palette size is four which means that the server sends 2 bit per pixel

                                    // Reset the current x and y coordinates
                                    currentTileX = 0
                                    currentTileY = 0

                                    // Calculate the count of total packed pixels
                                    packedPixelSize = ((tileWidth + 3) / 4) * tileHeight

                                    // If the buffer is to small reinitialize it
                                    if (packedPixels.size < packedPixelSize) packedPixels =
                                        ByteArray(packedPixelSize)

                                    // Fill the packed pixel buffer
                                    repeat(packedPixelSize) {
                                        packedPixels[packedPixelIndex++] =
                                            inflaterBuffer[currentPosition++]
                                    }

                                    // Iterate through each packed pixel
                                    repeat(packedPixelSize) { index ->
                                        for (i in 3 downTo 0) {
                                            // Iterate over the needed bits for the current encoding and set the pixel value for the current pixel
                                            rfbClient.bitmap[tileX + currentTileX, tileY + currentTileY] =
                                                cPixelPalette[packedPixels[index].toInt() shr (i * 2) and 3]
                                            //Color.rgb(0, 255, 0)

                                            if (tileX + ++currentTileX == tileX + tileWidth) {
                                                currentTileY++
                                                currentTileX = 0
                                                break
                                            }
                                        }

                                    }
                                }
                                else -> {
                                    // The palette size is larger than 4 which means that the server sends 4 bit per pixel

                                    // Reset the current x and y coordinates
                                    currentTileX = 0
                                    currentTileY = 0

                                    // Calculate the count of total packed pixels
                                    packedPixelSize = ((tileWidth + 1) / 2) * tileHeight

                                    // If the buffer is to small reinitialize it
                                    if (packedPixels.size < packedPixelSize) packedPixels =
                                        ByteArray(packedPixelSize)

                                    // Fill the packed pixel buffer
                                    repeat(packedPixelSize) {
                                        packedPixels[packedPixelIndex++] =
                                            inflaterBuffer[currentPosition++]
                                    }

                                    // Iterate through each packed pixel
                                    repeat(packedPixelSize) { index ->
                                        for (i in 1 downTo 0) {
                                            // Iterate over the needed bits for the current encoding and set the pixel value for the current pixel
                                            rfbClient.bitmap[tileX + currentTileX, tileY + currentTileY] =
                                                cPixelPalette[packedPixels[index].toInt() shr (i * 4) and 15]
                                            //Color.rgb(0, 0, 255)

                                            if (tileX + ++currentTileX == tileX + tileWidth) {
                                                currentTileY++
                                                currentTileX = 0
                                                break
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }

                } else {
                    // Tile is run length encoded
                    when (paletteSize) {
                        0 -> {
                            // The palette size is zero which means that the tile is plain run length encoded

                            // Reset the current x and y coordinates and the runComplete variable
                            currentTileX = 0
                            currentTileY = 0
                            runComplete = false

                            // Repeat until the run is complete
                            while (!runComplete) {
                                // Read the color of the current run
                                curBlue =
                                    0x00 shl 24 or (inflaterBuffer[currentPosition++].toInt() and 0xff)
                                curGreen =
                                    0x00 shl 24 or (inflaterBuffer[currentPosition++].toInt() and 0xff)
                                curRed =
                                    0x00 shl 24 or (inflaterBuffer[currentPosition++].toInt() and 0xff)

                                currentColor = Color.rgb(curRed, curGreen, curBlue)

                                // Fill as many pixels as the server sends with the length with the earlier read color
                                for (i in 0 until readRunLength()) {
                                    rfbClient.bitmap[tileX + currentTileX, tileY + currentTileY] =
                                        currentColor

                                    if (++currentTileX == tileWidth) {
                                        if (++currentTileY == tileHeight) {
                                            runComplete = true
                                            break
                                        }
                                        currentTileX = 0
                                    }
                                }
                            }
                        }
                        else -> {
                            // The palette size is larger than zero which means that the tile is palette run length encoded

                            // Reset the current x and y coordinates and the runComplete variable
                            currentTileX = 0
                            currentTileY = 0
                            runComplete = false

                            // Repeat until the run is complete
                            while (!runComplete) {
                                // Read a byte which gives information about the current run
                                currentByte = inflaterBuffer[currentPosition++]

                                // Read the palette index of the current color
                                currentColor = cPixelPalette[currentByte.toInt() and 127]


                                if (currentByte.toInt() and 128 != 0) {
                                    // The top bit is set which means that the run length is larger than one
                                    // Fill as many pixels as the server sends with the length with the color at the palette index
                                    for (i in 0 until readRunLength()) {
                                        rfbClient.bitmap[tileX + currentTileX, tileY + currentTileY] =
                                            currentColor

                                        if (++currentTileX == tileWidth) {
                                            if (++currentTileY == tileHeight) {
                                                runComplete = true
                                                break
                                            }
                                            currentTileX = 0
                                        }
                                    }
                                } else {
                                    // The top bit is not set which means that the length of the run is 1

                                    // Fill one pixel with the color at the given index
                                    rfbClient.bitmap[tileX + currentTileX, tileY + currentTileY] =
                                        currentColor

                                    if (++currentTileX == tileWidth) {
                                        currentTileY++
                                        if (currentTileY == tileHeight) {
                                            runComplete = true
                                            break
                                        }
                                        currentTileX = 0
                                    }
                                }
                            }
                        }
                    }
                }
                // Continue with the next tile (tiles are always 62 pixels wide but can we smaller if they are at the end)
                tileX += 64
            }
            // Continue with the next tile (tiles are always 62 pixels wide but can we smaller if they are at the end)
            tileY += 64
        }
    }

    private fun readPalette(pSize: Int) {
        // Read the palette
        // Start a position 0
        curPalettePosition = 0

        // Repeat palette size times
        repeat(pSize) {

            // Read the CPIXEL
            curBlue = 0x00 shl 24 or (inflaterBuffer[currentPosition++].toInt() and 0xff)
            curGreen = 0x00 shl 24 or (inflaterBuffer[currentPosition++].toInt() and 0xff)
            curRed = 0x00 shl 24 or (inflaterBuffer[currentPosition++].toInt() and 0xff)

            // Add the color to the palette
            cPixelPalette[curPalettePosition] = Color.rgb(curRed, curGreen, curBlue)
            // Go to the next position in the palette
            curPalettePosition++
        }
    }

    private fun readRunLength(): Int {
        // Read the length of the run
        // Since the length is defined as +1 at the end the value starts at 1
        runLength = 1
        // Read the byte to start with
        currentByte = inflaterBuffer[currentPosition++]

        // While the current byte is 255 we continue adding the bytes to the run length
        while (0x00 shl 24 or currentByte.toInt() and 0xFF == 255) {
            // Add byte to run length
            runLength += 0x00 shl 24 or currentByte.toInt() and 0xFF

            // Read the next byte
            currentByte = inflaterBuffer[currentPosition++]
        }

        // If a byte was not 255 add the byte and return the run length
        runLength += (0x00 shl 24 or currentByte.toInt() and 0xFF)

        return runLength
    }

}