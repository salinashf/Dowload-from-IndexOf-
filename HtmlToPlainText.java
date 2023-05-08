

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;
import org.jsoup.select.NodeTraversor;
import java.io.IOException;
import java.util.Iterator;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;


public class HtmlToPlainText
{
    private static final String userAgent = "Mozilla/5.0 (jsoup)";
    private static final int timeout = 5000;
    
    public static void main(final String... args) throws IOException {
        Validate.isTrue(args.length == 1 || args.length == 2, "usage: java -cp jsoup.jar org.jsoup.examples.HtmlToPlainText url [selector]");
        final String url = args[0];
        final String selector = (args.length == 2) ? args[1] : null;
        final Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (jsoup)").timeout(5000).get();
        final HtmlToPlainText formatter = new HtmlToPlainText();
        if (selector != null) {
            final Elements elements = doc.select(selector);
            for (final Element element : elements) {
                final String plainText = formatter.getPlainText(element);
                System.out.println(plainText);
            }
        }
        else {
            final String plainText2 = formatter.getPlainText((Element)doc);
            System.out.println(plainText2);
        }
    }
    
    public String getPlainText(final Element element) {
        final FormattingVisitor formatter = new FormattingVisitor((FormattingVisitor)null);
        final NodeTraversor traversor = new NodeTraversor((NodeVisitor)formatter);
        traversor.traverse((Node)element);
        return formatter.toString();
    }
    
    private class FormattingVisitor implements NodeVisitor
    {
        private static final int maxWidth = 80;
        private int width;
        private StringBuilder accum;
        
        private FormattingVisitor() {
            this.width = 0;
            this.accum = new StringBuilder();
        }
        
        public void head(final Node node, final int depth) {
            final String name = node.nodeName();
            if (node instanceof TextNode) {
                this.append(((TextNode)node).text());
            }
            else if (name.equals("li")) {
                this.append("\n * ");
            }
            else if (name.equals("dt")) {
                this.append("  ");
            }
            else if (StringUtil.in(name, new String[] { "p", "h1", "h2", "h3", "h4", "h5", "tr" })) {
                this.append("\n");
            }
        }
        
        public void tail(final Node node, final int depth) {
            final String name = node.nodeName();
            if (StringUtil.in(name, new String[] { "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5" })) {
                this.append("\n");
            }
            else if (name.equals("a")) {
                this.append(String.format(" <%s>", node.absUrl("href")));
            }
        }
        
        private void append(final String text) {
            if (text.startsWith("\n")) {
                this.width = 0;
            }
            if (text.equals(" ") && (this.accum.length() == 0 || StringUtil.in(this.accum.substring(this.accum.length() - 1), new String[] { " ", "\n" }))) {
                return;
            }
            if (text.length() + this.width > 80) {
                final String[] words = text.split("\\s+");
                for (int i = 0; i < words.length; ++i) {
                    String word = words[i];
                    final boolean last = i == words.length - 1;
                    if (!last) {
                        word = String.valueOf(word) + " ";
                    }
                    if (word.length() + this.width > 80) {
                        this.accum.append("\n").append(word);
                        this.width = word.length();
                    }
                    else {
                        this.accum.append(word);
                        this.width += word.length();
                    }
                }
            }
            else {
                this.accum.append(text);
                this.width += text.length();
            }
        }
        
        @Override
        public String toString() {
            return this.accum.toString();
        }
    }
}

