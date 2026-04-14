package com.example.blog.service;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarkdownService {

  private final Parser parser;
  private final HtmlRenderer renderer;

  public MarkdownService() {
    List<Extension> extensions = List.of(
        AutolinkExtension.create(),
        TablesExtension.create()
    );
    this.parser = Parser.builder()
        .extensions(extensions)
        .build();
    this.renderer = HtmlRenderer.builder()
        .extensions(extensions)
        .softbreak("<br />\n")
        .build();
  }

  public String render(String markdown) {
    if (markdown == null || markdown.isBlank()) {
      return "";
    }
    Node document = parser.parse(markdown);
    return renderer.render(document);
  }

  public String toPlainText(String markdown) {
    if (markdown == null || markdown.isBlank()) {
      return "";
    }
    String html = render(markdown);
    return html
        .replaceAll("(?i)<br\\s*/?>", " ")
        .replaceAll("(?i)</p>|</div>|</h[1-6]>|</li>|</tr>", " ")
        .replaceAll("<[^>]*>", " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replaceAll("\\s+", " ")
        .trim();
  }
}
