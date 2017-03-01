package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import java.io.File;
import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DocumentLoader {
  private static final FsFile.Filter ENDS_WITH_XML = new FsFile.Filter() {
    @Override public boolean accept(@NotNull FsFile fsFile) {
      return fsFile.getName().endsWith(".xml");
    }
  };

  private final FsFile resourceBase;
  private final String packageName;
  private final DocumentBuilderFactory documentBuilderFactory;

  public DocumentLoader(String packageName, ResourcePath resourcePath) {
    this.resourceBase = resourcePath.getResourceBase();
    this.packageName = packageName;

    documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    documentBuilderFactory.setIgnoringComments(true);
    documentBuilderFactory.setIgnoringElementContentWhitespace(true);
  }

  public void load(String folderBaseName, XmlLoader... xmlLoaders) {
    FsFile[] files = resourceBase.listFiles(new StartsWithFilter(folderBaseName));
    if (files == null) {
      throw new RuntimeException(resourceBase.join(folderBaseName) + " is not a directory");
    }
    for (FsFile dir : files) {
      loadFile(dir, xmlLoaders);
    }
  }

  private void loadFile(FsFile dir, XmlLoader[] xmlLoaders) {
    if (!dir.exists()) {
      throw new RuntimeException("no such directory " + dir);
    }

    for (FsFile file : dir.listFiles(ENDS_WITH_XML)) {
      loadResourceXmlFile(file, xmlLoaders);
    }
  }

  private void loadResourceXmlFile(FsFile fsFile, XmlLoader... xmlLoaders) {
    Document document = parse(fsFile);
    for (XmlLoader xmlLoader : xmlLoaders) {
      xmlLoader.processResourceXml(fsFile, document, packageName);
    }
  }

  private Document parse(FsFile xmlFile) {
    try {
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      return documentBuilder.parse(new ByteArrayInputStream(xmlFile.getBytes()));
    } catch (Exception e) {
      throw new RuntimeException("Error parsing " + xmlFile, e);
    }
  }
}
