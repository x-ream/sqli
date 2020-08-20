/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.xream.sqli.common.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * @Author Sim
 */
public class ClassFileReader {

	private ClassFileReader(){}

	public static Set<Class<?>> getClasses(String pack) {

		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

		try {
			Class<?> clz = Class.forName(pack);
			classes.add(clz);
			return classes;
		} catch (ClassNotFoundException e1) {
		}

		boolean recursive = true;

		String packageName = pack;
		String packageDirName = packageName.replace('.', '/');

		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);

			while (dirs.hasMoreElements()) {

				URL url = dirs.nextElement();

				String protocol = url.getProtocol();

				if ("file".equals(protocol)) {

					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");

					findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
				} else if ("jar".equals(protocol)) {

					JarFile jar;
					try {

						jar = ((JarURLConnection) url.openConnection()).getJarFile();

						Enumeration<JarEntry> entries = jar.entries();

						while (entries.hasMoreElements()) {
							JarEntry entry = entries.nextElement();
							String name = entry.getName();

							if (name.charAt(0) == '/') {
								name = name.substring(1);
							}

							if (name.startsWith(packageDirName)) {
								int idx = name.lastIndexOf('/');

								if (idx != -1) {

									packageName = name.substring(0, idx).replace('/', '.');
								}

								if ((idx != -1) || recursive) {

									if (name.endsWith(".class") && !entry.isDirectory()) {

										String className = name.substring(packageName.length() + 1, name.length() - 6);
										try {

											classes.add(Class.forName(packageName + '.' + className));
										} catch (ClassNotFoundException e) {

											e.printStackTrace();
										}
									}
								}
							}
						}
					} catch (IOException e) {

						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return classes;
	}

	private static void findAndAddClassesInPackageByFile(String packageName, String packagePath,
			final boolean recursive, Set<Class<?>> classes) {

		File dir = new File(packagePath);

		if (!dir.exists() || !dir.isDirectory()) {

			return;
		}

		File[] dirfiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));

		for (File file : dirfiles) {

			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive,
						classes);
			} else {

				String className = file.getName().substring(0, file.getName().length() - 6);
				try {

					classes.add(
							Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
				} catch (ClassNotFoundException e) {

					e.printStackTrace();
				}
			}
		}
	}

}
