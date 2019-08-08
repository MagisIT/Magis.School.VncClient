package de.magisit.vncclient.protocol.encodings.frame

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import de.magisit.vncclient.RfbClient
import de.magisit.vncclient.protocol.encodings.FrameEncoding
import de.magisit.vncclient.utils.ExtendedDataInputStream
import de.magisit.vncclient.utils.exceptions.RfbProtocolException
import java.util.zip.Inflater


class TightEncoding : FrameEncoding(encodingId = 7) {

    private val tightInflaters = arrayOfNulls<Inflater>(4)

    private var pixels = intArrayOf(10)
    private var currentColor: Int = 0

    // Initialize the encoding ids
    private val tightMaxSubEncoding = 0x09
    private val tightFillEncoding = 0x08
    private val tightJpegEncoding = 0x09
    private val tightExplicitFilter = 0x04
    private val tightFilterPalette = 0x01
    private val tightFilterCopy = 0x00
    private val tightFilterGradient = 0x02
    private val tightMinimumCompressionSize = 12

    // Initialize some buffers
    private val tightFillColorBuffer = ByteArray(3)
    private val tightColorBuffer = ByteArray(768)
    private val uncompressedDataBuffer = ByteArray(tightMinimumCompressionSize * 3)
    private var zLibData = ByteArray(4096)
    private var inflaterBuffer = ByteArray(8192)
    private val tighColorPalette24 = IntArray(256)

    // Variables initialized in the class to prevent heavy use of memory
    private var red: Int = 0
    private var green: Int = 0
    private var blue: Int = 0
    private var rowSize: Int = 0
    private var useGradient: Boolean = false
    private var dataSize: Int = 0
    private var numberOfColors: Int = 0
    private var filterId: Int = 0
    private var bitsPerPixel: Int = 0
    private var bytesPerPixel: Int = 0
    private var blueShift: Int = 0
    private var greenShift: Int = 0
    private var redShift: Int = 0
    private var pixelColorMask: Int = 0
    private var pixelData: Int = 0
    private var compressionControl: Int = 0
    private var jpegDataLength: Int = 0
    private var colorOffset: Int = 0
    private var positionInArray: Int = 0
    private var toUseStreamId: Int = 0
    private var currentX: Int = 0
    private var currentY: Int = 0
    private lateinit var inflater: Inflater
    private var xByteOffset: Int = 0
    private var yByteOffset: Int = 0
    private var n: Int = 0
    private var currentByte: Byte = 0
    private var streamId: Int = 0
    private var zLibDataLength: Int = 0
    private lateinit var tightPixels: IntArray
    private var rowBytes: Int = 0

    private lateinit var tightJpegBitmap: Bitmap
    private val bitmapOptions = BitmapFactory.Options()

    init {
        // TODO Replace the deprecated Options and prob replace the whole Bitmap, cause a Bitmap is Android only and the library should prob run on IOS too
        bitmapOptions.inPurgeable = false
        bitmapOptions.inDither = false
        bitmapOptions.inTempStorage = ByteArray(32768)
        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565
        bitmapOptions.inScaled = false
    }


    override fun readAndDecode(
        inputStream: ExtendedDataInputStream,
        rfbClient: RfbClient,
        width: Int,
        height: Int,
        xPosition: Int,
        yPosition: Int
    ) {

        pixels = IntArray(width * height)

        // Initialize the general info provided by the FrameBufferInfo object
        bitsPerPixel = rfbClient.frameBufferInfo.pixelFormat.bitsPerPixel
        bytesPerPixel = bitsPerPixel / 8
        blueShift = rfbClient.frameBufferInfo.pixelFormat.blueShift
        greenShift = rfbClient.frameBufferInfo.pixelFormat.greenShift
        redShift = rfbClient.frameBufferInfo.pixelFormat.redShift

        // Reset the variables
        rowSize = width
        useGradient = false
        numberOfColors = 0


        // Read the compression control byte from the input stream
        compressionControl = inputStream.readUnsignedByte()

        // Iterate through the first 4 bits and flush the zlib streams if told to do so
        streamId = 0
        while (streamId < 4) {
            if ((compressionControl and 1) != 0) {
                this.tightInflaters[streamId] = null
            }
            // Shift one bit further
            compressionControl = compressionControl shr 1
            streamId++
        }

        // If the compression control bit is larger than the maximum after flushing the streams and exception is thrown
        if (compressionControl > this.tightMaxSubEncoding) throw RfbProtocolException("Incorrect tight subencoding: $compressionControl")


        // Handle filled rectangles (decode one color and fill the given rectangle with it)
        // LSB is 1 and the value of the bits is 1000 (0x08)
        if (compressionControl == this.tightFillEncoding) {
            if (bytesPerPixel == 1) {
                // TODO Add support for that
            } else {
                // Reset the color mask
                pixelColorMask = 0

                // Set the pixel mask
                repeat(bitsPerPixel / 4) {
                    pixelColorMask = pixelColorMask or (1 shl it)
                }

                // If bytesPerPixel is not one the pixel data comes in 3 bytes: red, green, blue
                // Read the 3 bytes
                inputStream.readFully(tightFillColorBuffer, 0, 3)

                // Reset
                pixelData = 0

                // Add the values to pixel data
                pixelData = pixelData or (tightFillColorBuffer[0].toInt() and 0xFF shl (0 * 8))
                pixelData = pixelData or (tightFillColorBuffer[1].toInt() and 0xFF shl (1 * 8))
                pixelData = pixelData or (tightFillColorBuffer[2].toInt() and 0xFF shl (2 * 8))

                // Extract the color values from pixel data
                red = (pixelData shr blueShift) and pixelColorMask
                green = (pixelData shr greenShift) and pixelColorMask
                blue = (pixelData shr redShift) and pixelColorMask

                currentColor = Color.rgb(red, green, blue)

                repeat(width * height) {
                    pixels[it] = Color.rgb(red, green, blue)
                }

                rfbClient.bitmap.setPixels(pixels, 0, width, xPosition, yPosition, width, height)
                // Return cause the rectangle is fully handled
                return
            }
        }

        // Handle JPEG compression
        // LSB is 1 and the value of the bits is 1001 (0x09)
        if (compressionControl == this.tightJpegEncoding) {
            jpegDataLength = inputStream.readCompactLength()

            if (this.inflaterBuffer.size < jpegDataLength) this.inflaterBuffer =
                ByteArray(2 * jpegDataLength)

            inputStream.readFully(this.inflaterBuffer, 0, jpegDataLength)

            // Decode JPEG
            tightJpegBitmap =
                BitmapFactory.decodeByteArray(inflaterBuffer, 0, jpegDataLength, bitmapOptions)

            // TODO Creating a new bitmap every time a JPEG Frame comes is probably a bad idea in terms of memory and performance
            // Replace the specific part of the main bitmap with the new decoded jpeg information
            tightPixels = IntArray(width * height)
            tightJpegBitmap.getPixels(tightPixels, 0, width, 0, 0, width, height)
            rfbClient.bitmap.setPixels(tightPixels, 0, width, xPosition, yPosition, width, height)

            tightJpegBitmap.recycle()
            return
        }

        // Read filter id and parameters
        if ((compressionControl and this.tightExplicitFilter) != 0) {

            // Read the filter id
            filterId = inputStream.readUnsignedByte()

            // Handle the different filter
            when {
                // Handle the palette filter
                filterId == this.tightFilterPalette -> {
                    // Read the number of colors
                    numberOfColors = inputStream.readUnsignedByte() + 1

                    if (bytesPerPixel == 1) {
                        Log.d("INFO", "NOT IMPLEMENTED")
                    } else {
                        // Read the colorBuffer
                        inputStream.readFully(tightColorBuffer, 0, numberOfColors * 3)

                        // Reset the color mask
                        pixelColorMask = 0

                        // Set the pixel mask
                        repeat(bitsPerPixel / 4) {
                            pixelColorMask = pixelColorMask or (1 shl it)
                        }

                        // Initialize the pixel data
                        pixelData = 0
                        colorOffset = 0
                        positionInArray = 0

                        repeat(numberOfColors * 3) {
                            // Add the new byte to the existing pixel data
                            pixelData =
                                pixelData or (tightColorBuffer[it].toInt() and 0xFF shl (colorOffset * 8))

                            // If we read the needed 4 bytes (RGBA) shift them by the shift value and apply the color mask
                            if (++colorOffset == 3) {
                                red = (pixelData shr blueShift) and pixelColorMask
                                green = (pixelData shr greenShift) and pixelColorMask
                                blue = (pixelData shr redShift) and pixelColorMask

                                tighColorPalette24[positionInArray] = Color.rgb(red, green, blue)

                                positionInArray++
                                // Reset the pixel data and color offset
                                pixelData = 0
                                colorOffset = 0
                            }
                        }
                    }

                    if (numberOfColors == 2) {
                        rowSize = (width + 7) / 8
                    }

                }
                // Handle the gradient filter
                filterId == this.tightFilterGradient -> {
                    useGradient = true
                }
                // If the the filter id is neither palette or gradient nor copy there was an error and an exception is thrown
                filterId != this.tightFilterCopy -> throw RfbProtocolException("Incorrect tight filter id $filterId")
            }
        }

        if (numberOfColors == 0 && bytesPerPixel == 4) rowSize *= 3

        dataSize = height * rowSize


        // Data is to small to be compressed
        if (dataSize < this.tightMinimumCompressionSize) {
            inputStream.readFully(uncompressedDataBuffer, 0, dataSize)

            if (numberOfColors != 0) {
                // Indexed colors
                if (numberOfColors == 2) {
                    // Two colors
                    if (bytesPerPixel == 1) {

                        return
                        // Decode
                    } else {
                        decodeMonoData(
                            xPosition,
                            yPosition,
                            width,
                            height,
                            inflaterBuffer,
                            tighColorPalette24,
                            rfbClient
                        )
                        rfbClient.bitmap.setPixels(
                            pixels,
                            0,
                            width,
                            xPosition,
                            yPosition,
                            width,
                            height
                        )
                        return
                    }
                } else {
                    // Data is encoded in 8 bit (1 byte) because the palette is larger than two an can contain up to 256 colors
                    // Set pixels in bitmap

                    currentX = xPosition
                    currentY = yPosition

                    repeat(dataSize) {
                        pixels[width * (currentY - yPosition) + currentX - xPosition] =
                            tighColorPalette24[inflaterBuffer[it].toInt() and 0xFF]
                        currentX++


                        if (currentX == xPosition + width) {
                            currentX = xPosition
                            currentY++
                        }
                    }

                    return
                }
            } else if (useGradient) {
                Log.d("INFO", "NOT IMPLEMENTED")
                return
            } else {

            }
        } else {

            // Data was compressed with zlib and has to be decompressed
            zLibDataLength = inputStream.readCompactLength()

            if (zLibDataLength > zLibData.size) zLibData = ByteArray(zLibDataLength * 2)

            inputStream.readFully(zLibData, 0, zLibDataLength)

            toUseStreamId = compressionControl and 0x03
            if (tightInflaters[toUseStreamId] == null) tightInflaters[toUseStreamId] = Inflater()

            inflater = tightInflaters[toUseStreamId]!!

            inflater.setInput(zLibData, 0, zLibDataLength)

            if (dataSize > inflaterBuffer.size) inflaterBuffer = ByteArray(dataSize * 2)

            inflater.inflate(inflaterBuffer, 0, dataSize)


            if (numberOfColors != 0) {
                // The palette filter was applied
                if (numberOfColors == 2) {
                    // Data is encoded in 1 bit per pixel (0 or 1 because we have only 2 colors in the palette)
                    if (bytesPerPixel == 1) {
                        // TODO Add support for that
                        return
                    } else {
                        decodeMonoData(
                            xPosition,
                            yPosition,
                            width,
                            height,
                            inflaterBuffer,
                            tighColorPalette24,
                            rfbClient
                        )
                        rfbClient.bitmap.setPixels(
                            pixels,
                            0,
                            width,
                            xPosition,
                            yPosition,
                            width,
                            height
                        )
                        return
                    }
                } else {
                    // Data is encoded in 8 bit (1 byte) because the palette is larger than two an can contain up to 256 colors
                    // Set pixels in bitmap

                    currentX = xPosition
                    currentY = yPosition

                    repeat(dataSize) {

                        pixels[width * (currentY - yPosition) + currentX - xPosition] =
                            tighColorPalette24[inflaterBuffer[it].toInt() and 0xFF]

                        currentX++


                        if (currentX == xPosition + width) {
                            currentX = xPosition
                            currentY++
                        }
                    }
                    rfbClient.bitmap.setPixels(
                        pixels,
                        0,
                        width,
                        xPosition,
                        yPosition,
                        width,
                        height
                    )
                    return

                }
            } else if (useGradient) {
                Log.d("INFO", "NOT IMPLEMENTED")
            } else {
                // There was no filter applied on the data and it can be read as raw TPIXELS

                // Reset the color mask
                pixelColorMask = 0

                // Initialize the pixel data
                pixelData = 0
                colorOffset = 0

                // Set the pixel mask
                repeat(bitsPerPixel / 4) {
                    pixelColorMask = pixelColorMask or (1 shl it)
                }

                currentX = xPosition
                currentY = yPosition

                repeat(dataSize) {
                    // Add the new byte to the existing pixel data
                    pixelData =
                        pixelData or (inflaterBuffer[it].toInt() and 0xFF shl (colorOffset * 8))

                    // If we read the needed 3 bytes (RGB) shift them by the shift value and apply the color mask
                    if (++colorOffset == 3) {
                        red = (pixelData shr blueShift) and pixelColorMask
                        green = (pixelData shr greenShift) and pixelColorMask
                        blue = (pixelData shr redShift) and pixelColorMask

                        pixels[width * (currentY - yPosition) + currentX - xPosition] =
                            Color.rgb(red, green, blue)

                        currentX++

                        // Reset the pixel data and color offset
                        pixelData = 0
                        colorOffset = 0
                    }

                    if (currentX == xPosition + width) {
                        currentX = xPosition
                        currentY++
                    }
                }
            }
        }

        rfbClient.bitmap.setPixels(pixels, 0, width, xPosition, yPosition, width, height)
    }

    private fun decodeMonoData(
        xPosition: Int,
        yPosition: Int,
        width: Int,
        height: Int,
        src: ByteArray,
        palette: IntArray,
        rfbClient: RfbClient
    ) {
        // Calculate the bytes contained in one row of pixels
        rowBytes = (width + 7) / 8

        // Assign the current x and y values
        currentX = xPosition
        currentY = yPosition

        yByteOffset = 0
        while (yByteOffset < height) {
            // Since the last byte gets filled with zeros firstly iterate over the first bytes
            xByteOffset = 0
            while (xByteOffset < width / 8) {
                // Get the current byte from the given array
                currentByte = src[yByteOffset * rowBytes + xByteOffset]

                // Iterate over the bits in the current byte
                n = 7
                while (n >= 0) {
                    // Update the pixel at the current Position with the color at index 0 or 1 in palette
                    pixels[width * (currentY - yPosition) + currentX - xPosition] =
                        palette[currentByte.toInt() shr n and 1]

                    // Increment the current pixel value and decrement the current bit shift value
                    currentX++
                    n--
                }

                xByteOffset++
            }

            // Iterate over the last byte which is filled with zeros (if there are a few pixels missing in the row
            n = 7
            while (n >= 8 - width % 8) {
                pixels[width * (currentY - yPosition) + currentX - xPosition] =
                    palette[src[yByteOffset * rowBytes + xByteOffset].toInt() shr n and 1]
                currentX++
                n--
            }

            currentY++
            currentX = xPosition
            yByteOffset++
        }
    }

}






























