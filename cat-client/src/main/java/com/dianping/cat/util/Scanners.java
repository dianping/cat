package com.dianping.cat.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.dianping.cat.util.Scanners.IMatcher.Direction;

public class Scanners {
	public static DirScanner forDir() {
		return DirScanner.INSTANCE;
	}

	public static JarScanner forJar() {
		return JarScanner.INSTANCE;
	}

	public static ResourceScanner forResource() {
		return ResourceScanner.INSTANCE;
	}

	public static abstract class DirMatcher implements IMatcher<File> {
		@Override
		public boolean isDirEligible() {
			return true;
		}

		@Override
		public boolean isFileElegible() {
			return false;
		}
	}

	public enum DirScanner {
		INSTANCE;

		public List<File> scan(File base, IMatcher<File> matcher) {
			List<File> files = new ArrayList<File>();
			StringBuilder relativePath = new StringBuilder();

			scanForFiles(base, relativePath, matcher, false, files);

			return files;
		}

		private void scanForFiles(File base, StringBuilder relativePath, IMatcher<File> matcher, boolean foundFirst,
		      List<File> files) {
			int len = relativePath.length();
			File dir = len == 0 ? base : new File(base, relativePath.toString());
			String[] list = dir.list();

			if (list != null) {
				for (String item : list) {
					File child = new File(dir, item);

					if (len > 0) {
						relativePath.append('/');
					}

					relativePath.append(item);

					Direction direction = matcher.matches(base, relativePath.toString());

					if (direction == null) {
						direction = Direction.NEXT;
					}

					switch (direction) {
					case MATCHED:
						if (matcher.isDirEligible() && child.isDirectory()) {
							files.add(child);
						}

						if (matcher.isFileElegible() && child.isFile()) {
							files.add(child);
						}

						break;
					case DOWN:
						// for sub-folders
						scanForFiles(base, relativePath, matcher, foundFirst, files);
						break;
					default:
						break;
					}

					relativePath.setLength(len); // reset

					if (foundFirst && files.size() > 0) {
						break;
					}
				}
			}
		}

		public File scanForOne(File base, IMatcher<File> matcher) {
			List<File> files = new ArrayList<File>(1);
			StringBuilder relativePath = new StringBuilder();

			scanForFiles(base, relativePath, matcher, true, files);

			if (files.isEmpty()) {
				return null;
			} else {
				return files.get(0);
			}
		}
	}

	public static abstract class FileMatcher implements IMatcher<File> {
		@Override
		public boolean isDirEligible() {
			return false;
		}

		@Override
		public boolean isFileElegible() {
			return true;
		}
	}

	public static interface IMatcher<T> {
		public boolean isDirEligible();

		public boolean isFileElegible();

		public Direction matches(T base, String path);

		public enum Direction {
			MATCHED,

			DOWN,

			NEXT;

			public boolean isDown() {
				return this == DOWN;
			}

			public boolean isMatched() {
				return this == MATCHED;
			}

			public boolean isNext() {
				return this == NEXT;
			}
		}
	}

	public enum JarScanner {
		INSTANCE;

		public ZipEntry getEntry(String jarFileName, String name) {
			ZipFile zipFile = null;

			try {
				zipFile = new ZipFile(jarFileName);

				ZipEntry entry = zipFile.getEntry(name);

				return entry;
			} catch (IOException e1) {
				// ignore
			} finally {
				if (zipFile != null) {
					try {
						zipFile.close();
					} catch (IOException e) {
						// ignore it
					}
				}
			}

			return null;
		}

		public byte[] getEntryContent(String jarFileName, String entryPath) {
			byte[] bytes = null;
			ZipFile zipFile = null;

			try {
				zipFile = new ZipFile(jarFileName);
				ZipEntry entry = zipFile.getEntry(entryPath);

				if (entry != null) {
					InputStream inputStream = zipFile.getInputStream(entry);
					bytes = Files.forIO().readFrom(inputStream);
				}
			} catch (Exception e) {
				// ignore
			} finally {
				if (zipFile != null) {
					try {
						zipFile.close();
					} catch (Exception e) {
					}
				}
			}

			return bytes;
		}

		public boolean hasEntry(String jarFileName, String name) {
			return getEntry(jarFileName, name) != null;
		}

		public List<String> scan(File jarFile, IMatcher<ZipEntry> matcher) throws ZipException, IOException {
			return scan(new ZipFile(jarFile), matcher);
		}

		public List<String> scan(ZipFile zipFile, IMatcher<ZipEntry> matcher) {
			List<String> files = new ArrayList<String>();

			scanZipFile(zipFile, matcher, false, files);
			return files;
		}

		public String scanForOne(File jarFile, IMatcher<ZipEntry> matcher) throws ZipException, IOException {
			List<String> files = new ArrayList<String>(1);

			scanZipFile(new ZipFile(jarFile), matcher, false, files);

			if (files.isEmpty()) {
				return null;
			} else {
				return files.get(0);
			}
		}

		private void scanZipFile(ZipFile zipFile, IMatcher<ZipEntry> matcher, boolean foundFirst, List<String> names) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();

				if (matcher.isDirEligible() && entry.isDirectory()) {
					IMatcher.Direction direction = matcher.matches(entry, name);

					if (direction.isMatched()) {
						names.add(name);
					}
				} else if (matcher.isFileElegible() && !entry.isDirectory()) {
					IMatcher.Direction direction = matcher.matches(entry, name);

					if (direction.isMatched()) {
						names.add(name);
					}
				}

				if (foundFirst && names.size() > 0) {
					break;
				}
			}
		}
	}

	public static abstract class ResourceMatcher implements IMatcher<URL> {
		@Override
		public boolean isDirEligible() {
			return false;
		}

		@Override
		public boolean isFileElegible() {
			return true;
		}
	}

	public enum ResourceScanner {
		INSTANCE;

		@SuppressWarnings("deprecation")
		private String decode(String url) {
			try {
				return URLDecoder.decode(url, "utf-8");
			} catch (UnsupportedEncodingException e) {
				return URLDecoder.decode(url);
			}
		}

		private void scan(Set<URL> done, final List<URL> urls, final URL base, String resourceBase,
		      final ResourceMatcher matcher) throws IOException {
			if (done.contains(base)) {
				return;
			} else {
				done.add(base);
			}

			String protocol = base.getProtocol();

			if ("file".equals(protocol)) { // local file folder
				scanFile(urls, base, matcher);
			} else if ("jar".equals(protocol)) { // normal jar file
				scanJar(urls, base, resourceBase, matcher);
			} else if ("zip".equals(protocol)) { // weblogic jar, zip file
				scanZip(urls, base, resourceBase, matcher);
			} else if ("wsjar".equals(protocol)) { // websphere jar, zip file
				scanZip(urls, base, resourceBase, matcher);
			}
		}

		public List<URL> scan(String resourceBase, final ResourceMatcher matcher) throws IOException {
			List<URL> urls = new ArrayList<URL>();
			Set<URL> done = new HashSet<URL>();

			// try to load from current class's classloader
			ClassLoader classLoader = getClass().getClassLoader();

			if (classLoader != null) {
				Enumeration<URL> e1 = classLoader.getResources(resourceBase);

				while (e1.hasMoreElements()) {
					scan(done, urls, e1.nextElement(), resourceBase, matcher);
				}
			}

			// try to load from current context's classloader
			Enumeration<URL> e2 = Thread.currentThread().getContextClassLoader().getResources(resourceBase);

			while (e2.hasMoreElements()) {
				scan(done, urls, e2.nextElement(), resourceBase, matcher);
			}

			return urls;
		}

		private void scanFile(final List<URL> urls, final URL base, final ResourceMatcher matcher) {
			File baseDir = new File(decode(base.getPath()));

			DirScanner.INSTANCE.scan(baseDir, new FileMatcher() {
				@Override
				public Direction matches(File dir, String path) {
					try {
						Direction d = matcher.matches(base, path);

						if (d.isMatched()) {
							String baseUrl = base.toExternalForm();
							String url;

							if (baseUrl.endsWith("/")) {
								url = baseUrl + path;
							} else {
								url = baseUrl + "/" + path;
							}

							urls.add(new URL(url));
						}

						return d;
					} catch (MalformedURLException e) {
						// ignore it
					}

					return Direction.DOWN;
				}
			});
		}

		private void scanJar(final List<URL> urls, final URL base, String resourceBase, final ResourceMatcher matcher)
		      throws IOException {
			String url = base.toExternalForm();
			int pos = url.lastIndexOf("!/");
			final URL u = new URL(url.substring(0, pos + 2));
			URLConnection conn = u.openConnection();

			if (conn instanceof JarURLConnection) {
				JarFile jarFile = ((JarURLConnection) conn).getJarFile();
				final String prefix = resourceBase + "/";

				try {
					JarScanner.INSTANCE.scan(jarFile, new ZipEntryMatcher() {
						@Override
						public boolean isDirEligible() {
							return matcher.isDirEligible();
						}

						@Override
						public boolean isFileElegible() {
							return matcher.isFileElegible();
						}

						@Override
						public Direction matches(ZipEntry entry, String path) {
							if (path.startsWith(prefix)) {
								try {
									String p = path.substring(prefix.length());
									Direction d = matcher.matches(base, p);

									if (d.isMatched()) {
										URL url = new URL(base.toExternalForm() + "/" + p);

										urls.add(url);
									}

									return d;
								} catch (MalformedURLException e) {
									// ignore it
								}
							}

							return Direction.DOWN;
						}
					});
				} finally {
					try {
						jarFile.close();
					} catch (Throwable e) {
						// ignore it
					}
				}
			}
		}

		private void scanZip(final List<URL> urls, final URL base, final String resourceBase,
		      final ResourceMatcher matcher) throws IOException {
			String path = base.getPath();
			int pos = path.lastIndexOf("!/");
			File jarFile = new File(path.substring("file:".length(), pos));
			final String prefix = resourceBase + "/";

			JarScanner.INSTANCE.scan(jarFile, new ZipEntryMatcher() {
				@Override
				public Direction matches(ZipEntry entry, String path) {
					if (path.startsWith(prefix)) {
						try {
							String p = path.substring(prefix.length());
							Direction d = matcher.matches(base, p);

							if (d.isMatched()) {
								URL url = new URL(base.toExternalForm() + "/" + p);

								urls.add(url);
							}

							return d;
						} catch (MalformedURLException e) {
							// ignore it
						}
					}

					return Direction.DOWN;
				}
			});
		}
	}

	public static abstract class ZipEntryMatcher implements IMatcher<ZipEntry> {
		@Override
		public boolean isDirEligible() {
			return false;
		}

		@Override
		public boolean isFileElegible() {
			return true;
		}
	}
}
