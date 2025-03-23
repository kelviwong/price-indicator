package storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.PriceService;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class MemoryMapFileStore<T> implements IStore<T> {
    protected static final Logger logger = LoggerFactory.getLogger(MemoryMapFileStore.class);
    protected final MappedByteBuffer buffer;
    private final String path;
    private final RandomAccessFile file;

    public MemoryMapFileStore(String path, int size) throws Exception {
        this.path = path;
        try {
            Path filePath = Paths.get(path);
            if (Files.exists(filePath)) {
                cleanUp(path);
            }

            file = new RandomAccessFile(this.path, "rw");
            FileChannel channel = file.getChannel();
            // Map a region of the file into memory
            buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, size);
        } catch (Exception e) {
            logger.error("Error initialize Memory Map File Store", e);
            throw new Exception("File Error");
        }
    }

    @Override
    public void close() {
        try {
            if (file != null) {
                file.getChannel().close();
                file.close();
            }
        } catch (Exception e) {
            logger.error("Error Memory Map File Store", e);
        }
    }

    public static void cleanUp(String filePath) {
        try {
            // Step 5: Delete the file
            Path path = Paths.get(filePath);
            if (Files.deleteIfExists(path)) {
                logger.info("File deleted successfully: " + filePath);
            } else {
                logger.info("File not found or could not be deleted: " + filePath);
            }
        } catch (IOException e) {
            logger.info("Error deleting file: " + e.getMessage());
        }
    }
}
