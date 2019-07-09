package com.dianping.cat.util;

import com.dianping.cat.message.CodecHandler;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.xerial.snappy.SnappyInputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * <p>
 * DumpDecompressor is used for decompressing CAT data found in the /data/appdatas/bucket/dump directory.
 * </p>
 *
 * @author Zheng Xu
 **/
public class DumpDecompressor {


    /**
     * decompresses a cat data file given its dat and idx files.
     *
     * @param dataFilePath
     * @param indexFilePath
     * @param type
     * @throws IOException
     */
    public static void decompress(String dataFilePath, String indexFilePath, DecompressorType type) throws IOException {

        byte[] indexFileBytes = Files.readAllBytes(Paths.get(indexFilePath));
<<<<<<< HEAD
<<<<<<< HEAD
        RandomAccessFile dataFile = new RandomAccessFile(dataFilePath, "r");
        for (int i = 8 * 4096; i < indexFileBytes.length; i = i + 8) {

=======
        for (int i = 8 * 4096; i < indexFileBytes.length; i = i + 8) {
            RandomAccessFile dataFile = new RandomAccessFile(dataFilePath, "r");
>>>>>>> a4fc8a3... 　cat dump file decompressor
=======
        RandomAccessFile dataFile = new RandomAccessFile(dataFilePath, "r");
        for (int i = 8 * 4096; i < indexFileBytes.length; i = i + 8) {

>>>>>>> 751cd29... move datastream out of the loop
            long secondaryIndex = getSecondaryIndex(Arrays.copyOfRange(indexFileBytes, i, i + 8));
            long blockAddress = secondaryIndex >> 24;
            int offSet = (int) (secondaryIndex & 0xFFFFFFL);


            dataFile.seek(blockAddress);
            int len = dataFile.readInt();
            if (len <= 0) {
                continue;
            }

            byte[] buf = new byte[len];
            dataFile.readFully(buf);

            DataInputStream in = new DataInputStream(new SnappyInputStream(new ByteArrayInputStream(buf)));
            in.skip(offSet);

            len = in.readInt();
            buf = new byte[len];
            in.readFully(buf);


            ByteBuf result = ByteBufAllocator.DEFAULT.buffer(4 + buf.length);
            result.writeInt(buf.length);
            result.writeBytes(buf);


            MessageTree messageTree = loadMessageTree(result);
            result.release();
            printMessageTree(messageTree, type);
<<<<<<< HEAD
<<<<<<< HEAD
            in.close();
        }
        dataFile.close();
=======
            dataFile.close();
        }
>>>>>>> a4fc8a3... 　cat dump file decompressor
=======
            in.close();
        }
        dataFile.close();
>>>>>>> 751cd29... move datastream out of the loop
    }

    static void printMessageTree(MessageTree tree, DecompressorType type) {
        switch (type) {
            case TRANSACTION:

                for (Transaction trax : tree.getTransactions()) {
                    System.out.println(trax.toString());
                }
                break;
            case EVENT:
                for (Event event : tree.getEvents()) {
                    System.out.println(event.toString());

                }
                break;
        }
        System.out.println("next message tree");
    }

    private static MessageTree loadMessageTree(ByteBuf buffer) {
        MessageTree tree = CodecHandler.decode(buffer);
        return tree;
    }

    private static long getSecondaryIndex(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getLong();
    }

    public enum DecompressorType {
        TRANSACTION, EVENT
    }

}