package com.dianping.cat.hadoop.hdfs;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.protocol.AlreadyBeingCreatedException;
import org.apache.hadoop.ipc.RemoteException;
import org.apache.hadoop.security.AccessControlException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;
import org.unidal.helper.Files.AutoClose;
import org.unidal.helper.Formats;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

public class HdfsUploader implements LogEnabled, Initializable {

	@Inject
	private FileSystemManager m_fileSystemManager;

    private int m_threads = 5;

    private ReentrantLock lock = new ReentrantLock();

    private TreeSet<FileAndPath> m_fileAndPathSet = new TreeSet<FileAndPath>();

    private TreeSet<FileAndPath> m_processingSet = new TreeSet<FileAndPath>();

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private FSDataOutputStream makeHdfsOutputStream(String path) throws IOException {
		StringBuilder baseDir = new StringBuilder(32);
		FileSystem fs = m_fileSystemManager.getFileSystem(ServerConfigManager.DUMP_DIR, baseDir);
		Path file = new Path(baseDir.toString(), path);
		FSDataOutputStream out;

		try {
			out = fs.create(file);
		} catch (RemoteException re) {
			fs.delete(file, false);

			out = fs.create(file);
		} catch (AlreadyBeingCreatedException e) {
			fs.delete(file, false);

			out = fs.create(file);
		}
		return out;
	}

    public void uploadLogviewFile(String path, File file) {
        FileAndPath fileAndPath = new FileAndPath(file,path);
        lock.lock();
        try {
            if (!m_fileAndPathSet.contains(fileAndPath) && !m_processingSet.contains(fileAndPath)){
                m_fileAndPathSet.add(fileAndPath);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void initialize() throws InitializationException {
        for (int i = 0; i < m_threads; i++) {
            Threads.forGroup("cat").start(new Upload(i));
        }
    }
    public class Upload implements Threads.Task {

        private int m_index;

        public Upload(int m_index) {
            this.m_index = m_index;
        }

        @Override
        public String getName() {
            return "Upload-" + m_index;
        }

        @Override
        public void shutdown() {

        }

        @Override
        public void run() {
            while (true) {
                FileAndPath item = null;

                lock.lock();
                try {
                    item = m_fileAndPathSet.pollFirst();
                    if(item != null){
                        m_processingSet.add(item);
                    }
                } finally {
                    lock.unlock();
                }

                if (item != null) {
                    if(!item.getFile().exists()){
                        continue;
                    }

                    Transaction t = Cat.newTransaction("System", "UploadDump");
                    t.addData("file", item.getPath());

                    FSDataOutputStream fdos = null;
                    FileInputStream fis = null;
                    try {
                        fdos = makeHdfsOutputStream(item.getPath());
                        fis = new FileInputStream(item.getFile());

                        long start = System.currentTimeMillis();

                        Files.forIO().copy(fis, fdos, AutoClose.INPUT_OUTPUT);

                        double sec = (System.currentTimeMillis() - start) / 1000d;
                        String size = Formats.forNumber().format(item.getFile().length(), "0.#", "B");
                        String speed = sec <= 0 ? "N/A" : Formats.forNumber().format(item.getFile().length() / sec, "0.0", "B/s");

                        t.addData("size", size);
                        t.addData("speed", speed);
                        t.setStatus(Message.SUCCESS);

                        deleteFile(item.getFile());
                    } catch (AlreadyBeingCreatedException e) {
                        Cat.logError(e);
                        t.setStatus(e);

                        m_logger.error(String.format("Already being created (%s)!", item.getPath()), e);
                    } catch (AccessControlException e) {
                        Cat.logError(e);
                        t.setStatus(e);
                        m_logger.error(String.format("No permission to create HDFS file(%s)!", item.getPath()), e);
                    } catch (Exception e) {
                        Cat.logError(e);
                        t.setStatus(e);
                        m_logger.error(String.format("Uploading file(%s) to HDFS(%s) failed!", item.getFile(), item.getPath()), e);
                    } finally {
                        try {
                            if (fdos != null) {
                                fdos.close();
                            }
                        } catch (IOException e) {
                            Cat.logError(e);
                        }
                        t.complete();

                        lock.lock();
                        try {
                            m_processingSet.remove(item);
                        } finally {
                            lock.unlock();
                        }
                    }
                }else{
                    try {
                        Thread.sleep(30 * 1000l);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void deleteFile(File file){
        if (!file.delete()) {
            m_logger.warn("Can't delete file: " + file);
        }
        file.getParentFile().delete();// delete it if empty
        file.getParentFile().getParentFile().delete();// delete it if empty
    }

    class FileAndPath implements Comparable{

        private File file;
        private String path;

        public FileAndPath(File file, String path) {
            this.file = file;
            this.path = path;
        }

        public File getFile() {
            return file;
        }

        public String getPath() {
            return path;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public int compareTo(Object o) {
            return this.path.compareTo(((FileAndPath) o).getPath());
        }
    }
}
