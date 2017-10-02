/*
 * MIT License
 *
 * Copyright (c) 2017 Svistunov Aleksey
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package name.svistun.photo;

import com.drew.imaging.ImageProcessingException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


public class PhotoRename {
    private static String dateFormat = "yyyyMMddHHmmss";
    private static String photoDirPath;
    private static int maxDepth = -1;
    private static boolean dryRun;
    private static String extensions = "jpeg,jpg,nef";
    private static Pattern photoFilePattern;
    private static String help = "" +
            "PhotoRename program is intended to rename photo files as you set " +
            "with Java date pattern" + System.lineSeparator() +
            "Usage:" + System.lineSeparator() +
            "<path> [options]" + System.lineSeparator() +
            "\t <path>            :    Path where photos to be renamed" + System.lineSeparator() +
            "Options:" + System.lineSeparator() +
            "\t -df --date-format :    Java patterned date format which files to be renamed in" + System.lineSeparator() +
            "\t                        https://docs.oracle.com/javase/8/docs/api/index.html" + System.lineSeparator() +
            "\t                        default is 'yyyyMMddHHmmss'" + System.lineSeparator() +
            "\t -md --max-depth   :    Maximum depth of inner folders to scan fo photo files" + System.lineSeparator() +
            "\t                        default is infinity" + System.lineSeparator() +
            "\t -dr --dry-run     :    Just output how rename will occur." + System.lineSeparator() +
            "\t -e --extension    :    Comma separated list of extensions of photos to rename." + System.lineSeparator() +
            "\t                        default is 'jpeg,jpg,nef'" + System.lineSeparator() +
            "\t -h --help         :    Print help message";
    private static SimpleDateFormat sdf;

    public static void main(String[] args) {
        processArgs(args);
        StringBuilder extsPatternSb = new StringBuilder();
        for (String extension : extensions.split(",")) {
            if (extsPatternSb.length() > 0) {
                extsPatternSb.append("|");
            }
            extsPatternSb.append(String.format("%s|%s", extension, extension.toUpperCase()));
        }
        photoFilePattern = Pattern.compile(String.format("(.+?)(\\.(%s))", extsPatternSb));
        try {
            List<Photo> photos = scanDir(photoDirPath);
            for (Photo photo : photos) {
                try {
                    if (!photo.rename(dryRun, sdf)) {
                        System.err.println(photo + " was not renamed.");
                    }
                } catch (IOException | ImageProcessingException ex) {
                    System.err.println(photo + " was not renamed.");
                    System.err.println(ex.toString() + System.lineSeparator()
                            + StringUtils.join(ex.getStackTrace(), System.lineSeparator()));
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.toString() + System.lineSeparator()
                    + StringUtils.join(ex.getStackTrace(), System.lineSeparator()));
            System.exit(1);
        }
    }

    private static void processArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-df":
                    i++;
                    dateFormat = args[i];
                    break;
                case "--date-format":
                    i++;
                    dateFormat = args[i];
                    break;
                case "-e":
                    i++;
                    extensions = args[i];
                    break;
                case "--extension":
                    i++;
                    extensions = args[i];
                    break;
                case "-md":
                    i++;
                    maxDepth = Integer.parseInt(args[i]);
                    break;
                case "--max-depth":
                    i++;
                    maxDepth = Integer.parseInt(args[i]);
                    break;
                case "-dr":
                    dryRun = true;
                    break;
                case "--dry-run":
                    dryRun = true;
                    break;
                case "-h":
                    System.out.println(help);
                    System.exit(0);
                case "--help":
                    System.out.println(help);
                    System.exit(0);
                default:
                    photoDirPath = args[i];
                    break;
            }
        }
        if (null == photoDirPath) {
            System.err.println(help);
            System.exit(1);
        }
        sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(TimeZone.getTimeZone("GTM"));
        File photoDir = new File(photoDirPath);
        if (! photoDir.isDirectory()) {
            System.err.println(String.format("[%s] is not a directory. Exiting.", photoDirPath));
            System.exit(1);
        }
    }

    private static List<Photo> scanDir(String photoDirPath) throws IOException {
        List<Photo> photos = new ArrayList<>();
        List<File> dirs = new ArrayList<>();
        dirs.add(new File(photoDirPath));
        while (! dirs.isEmpty() && maxDepth != 0) {
            List<File> newDirs = new ArrayList<>();
            for (File dir : dirs) {
                File[] dirFiles = dir.listFiles();
                if (null == dirFiles) {
                    System.err.println(String.format("Dir %s returned null at getting list of its files.", dir.getAbsolutePath()));
                    continue;
                }
                for (File file : dirFiles) {
                    if (file.isDirectory()) {
                        newDirs.add(file);
                    } else {
                        Matcher matcherPhoto = photoFilePattern.matcher(file.getName());
                        if (matcherPhoto.matches()) {
                            photos.add(new Photo(file));
                        }
                    }
                }
            }
            dirs = newDirs;
            maxDepth--;
        }
        return photos;
    }
}
