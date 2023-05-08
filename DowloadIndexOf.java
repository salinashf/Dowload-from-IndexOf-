

import java.net.URLConnection;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.net.URL;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;
import java.io.IOException;
import java.util.Iterator;
import java.util.ArrayList;


public class DowloadIndexOf
{
    public static String nameFileUrl;
    static ArrayList<String> exclude;
    
    static {
        DowloadIndexOf.nameFileUrl = "Label";
        (DowloadIndexOf.exclude = new ArrayList<String>()).add("Name");
        DowloadIndexOf.exclude.add("Last modified");
        DowloadIndexOf.exclude.add("Size");
        DowloadIndexOf.exclude.add("Description");
        DowloadIndexOf.exclude.add("Parent Directory");
    }
    
    public static boolean contains(final String test) {
        for (final String c : DowloadIndexOf.exclude) {
            if (c.equals(test)) {
                return true;
            }
        }
        return false;
    }
    
    public static void main(final String[] args) throws IOException {
        final String url = args[0];
        final String paths = args[1];
        DowloadIndexOf.nameFileUrl = args[2];
        list(url, paths);
    }
    
    public static String getSubFolder(String str) {
        if (str.length() > 0 && str.charAt(str.length() - 1) == '/') {
            str = str.substring(0, str.length() - 1);
        }
        str = str.substring(str.lastIndexOf(47) + 1, str.length());
        str = str.replaceAll("%20", " ");
        return str;
    }
    
    public static String getSubName(String str) {
        if (str.length() > 0 && str.charAt(str.length() - 1) != '/') {
            str = str.substring(0, str.length());
        }
        str = str.substring(str.lastIndexOf(47) + 1, str.length());
        str = str.replaceAll("%20", " ");
        return str;
    }
    
    public static void list(final String url, final String Path) {
        try {
            final Document doc = Jsoup.connect(url).get();
            final Elements links = doc.select("a[href]");
            for (final Element link : links) {
                if (!contains(link.text())) {
                    if (link.attr("abs:href").endsWith("/")) {
                        System.out.println("Directorio web " + link.attr("abs:href"));
                        final String folder = getSubFolder(link.attr("abs:href"));
                        System.out.println("Directorio Sub Folder " + folder);
                        System.out.println("Directorio Completp Folder " + Path + folder + "/");
                        list(link.attr("abs:href"), String.valueOf(Path) + folder + "/");
                    }
                    else if (DowloadIndexOf.nameFileUrl.equalsIgnoreCase("Label")) {
                        Download(link.attr("abs:href"), link.text(), Path);
                    }
                    else {
                        if (!DowloadIndexOf.nameFileUrl.equalsIgnoreCase("Url")) {
                            continue;
                        }
                        Download(link.attr("abs:href"), getSubName(link.attr("abs:href")), Path);
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void Download(final String url, final String nameFile, final String Path) {
        final String sep = (Path.endsWith("/") | Path.endsWith("\\")) ? "" : "";
        try {
            System.out.println("Descargando url " + url);
            System.out.println("Descargando archivo  " + nameFile);
            System.out.println("Descargando el ruta   " + Path);
            InputStream in = new URL(url).openStream();
            final URL urlDw = new URL(url);
            final URLConnection connection = urlDw.openConnection();
            connection.connect();
            final int fileLenth = connection.getContentLength();
            in = urlDw.openStream();
            final File f = new File(String.valueOf(Path) + sep);
            f.mkdirs();
            final FileOutputStream fos = new FileOutputStream(new File(String.valueOf(Path) + sep + nameFile));
            int length = -1;
            final byte[] buffer = new byte[10240];
            long total = 0L;
            System.out.println("Total  " + total + " de " + fileLenth);
            while ((length = in.read(buffer)) > -1) {
                total += length;
                fos.write(buffer, 0, length);
            }
            fos.close();
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

