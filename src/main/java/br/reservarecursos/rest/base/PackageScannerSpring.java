/**
 * Copyright 2015 - Tássio Guerreiro Antunes Virgínio
 *
 * Este arquivo é parte do programa Reserva de Recursos
 *
 * O Reserva de Recursos é um software livre; você pode redistribui-lo e/ou modifica-lo
 * dentro dos termos da Licença Pública Geral GNU como publicada pela
 * Fundação do Software Livre (FSF); na versão 2 da Licença.
 *
 * Este programa é distribuido na esperança que possa ser util, mas SEM
 * NENHUMA GARANTIA; sem uma garantia implicita de ADEQUAÇÂO a qualquer
 * MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU
 * para maiores detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU, sob o
 * título "licensa_uso.htm", junto com este programa, se não, escreva para a
 * Fundação do Software Livre(FSF) Inc., 51 Franklin St, Fifth Floor,
 */
package br.reservarecursos.rest.base;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.wicketstuff.rest.annotations.ResourcePath;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by tassio on 28/11/15.
 */
@Component
public class PackageScannerSpring {

    private static final Logger log = LoggerFactory.getLogger(PackageScannerSpring.class);

    @Autowired
    private ApplicationContext applicationContext;

    public PackageScannerSpring() {
    }

    public void scanPackage(String... packageNames) {
        Args.notNull(packageNames, "packageNames");
        scanPackage(WebApplication.get(), packageNames);
    }

    public void scanPackage(WebApplication application, String... packageNames) {
        Args.notNull(application, "application");
        Args.notNull(packageNames, "packageNames");
        for (String packageName : packageNames) {
            scanPackage(application, packageName);
        }
    }

    public void scanPackage(WebApplication application, String packageName) {
        Args.notNull(application, "application");
        Args.notNull(packageName, "packageName");

        try {
            for (Class<?> clazz : getClasses(packageName)) {
                mountAnnotatedResource(application, clazz);
            }
        } catch (Exception e) {
            throw new WicketRuntimeException(e);
        }
    }

    private void mountAnnotatedResource(WebApplication application, Class<?> clazz) throws InstantiationException, IllegalAccessException {
        ResourcePath mountAnnotation = (ResourcePath) clazz.getAnnotation(ResourcePath.class);
        if (mountAnnotation != null && IResource.class.isAssignableFrom(clazz)) {
            String path = mountAnnotation.value();
            final IResource resourceInstance = (IResource) applicationContext.getBean(clazz);
            application.mountResource(path, new ResourceReference(clazz.getSimpleName()) {
                public IResource getResource() {
                    return resourceInstance;
                }
            });
            log.info("Resource \'" + clazz.getSimpleName() + "\' has been mounted to path \'" + path + "\'");
        }
    }

    private Class<?>[] getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Args.notNull(classLoader, "classLoader");
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        List<JarFile> jars = new ArrayList<JarFile>();

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            String protocol = url.getProtocol();
            if (!"jar".equals(protocol) && !"wsjar".equals(protocol)) {
                dirs.add(new File(url.getFile()));
            } else {
                String jarFile = URLDecoder.decode(url.getFile(), "UTF-8");
                jarFile = jarFile.substring(5, jarFile.indexOf("!"));
                jars.add(new JarFile(jarFile));
            }
        }

        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (File dir : dirs) {
            classes.addAll(findClasses(dir, packageName));
        }
        for (JarFile jar : jars) {
            classes.addAll(findClasses(jar, path));
        }
        return classes.toArray(new Class<?>[classes.size()]);
    }

    private List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    private List<Class<?>> findClasses(JarFile jarFile, String path) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        Enumeration<? extends JarEntry> jarEntries = jarFile.entries();

        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String entryName = jarEntry.getName();
            int classExtensionIndex = entryName.indexOf(".class");
            if (entryName.startsWith(path) && classExtensionIndex >= 0) {
                entryName = entryName.substring(0, classExtensionIndex);
                entryName = entryName.replace('/', '.');
                classes.add(Class.forName(entryName));
            }
        }
        return classes;
    }


}
