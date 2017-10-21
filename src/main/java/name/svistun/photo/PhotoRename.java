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

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;


public class PhotoRename {
    private static String dateFormat;
    private static boolean dryRun;
    private static int maxDepth;
    private static String[] photoDirPaths;
    private static SimpleDateFormat sdf;

    static {
        dateFormat = "yyyyMMdd'at'HHmm''ss";
    }

    public static void main(String[] args) {
        init(args);
        execute();
    }

    private static void init(String[] args){
        processArgs(args);
    }

    private static List<Photo> scanDir() throws IOException {
        List<Photo> photos = new ArrayList<>();
        List<File> dirs = new ArrayList<>();
        for (String photoDirPath : photoDirPaths) {
            dirs.add(new File(photoDirPath));
        }
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
                        try {
                            photos.add(new Photo(file));
                        } catch (ImageProcessingException e) {
                            System.err.println(String.format("File %s, %s", file.getAbsolutePath(), e.getMessage()));
                        } catch (NotImageFileException e) {
                            System.out.println(String.format("Skip processing file %s as an image, %s", file.getAbsolutePath(), e.getMessage()));
                        }
                    }
                }
            }
            dirs = newDirs;
            maxDepth--;
        }
        return photos;
    }

    private static void execute() {
        List<Photo> failedPhotos = new ArrayList<>();
        try {
            List<Photo> photos = scanDir();
            for (Photo photo : photos) {
                try {
                    if (!photo.rename(dryRun, sdf)) {
                        System.err.println(photo + " was not renamed.");
                        failedPhotos.add(photo);
                    }
                } catch (IOException | ImageProcessingException ex) {
                    System.err.println(photo + " was not renamed.");
                    System.err.println(ex.toString() + System.lineSeparator()
                            + StringUtils.join(ex.getStackTrace(), System.lineSeparator()));
                    failedPhotos.add(photo);
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.toString() + System.lineSeparator()
                    + StringUtils.join(ex.getStackTrace(), System.lineSeparator()));
            System.exit(1);
        }
        if (failedPhotos.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to rename photos:").append(System.lineSeparator());
            for (Photo failedPhoto : failedPhotos) {
                sb.append(String.format("\t%s" + System.lineSeparator(), failedPhoto));
            }
            System.err.println(sb);
        }
    }

    private static void processArgs(String[] args) {
        Options options = new Options();
        options.addOption(new Option("df", "date-format", true, String.format("Java patterned date format which files to be renamed in " +
                "https://docs.oracle.com/javase/8/docs/api/index.html " +
                "default is \"%s\"", dateFormat)));
        options.addOption(new Option("dr", "dry-run", false, "Just output how rename will occur"));
        options.addOption("h", "help", false, "Print help message");
        options.addOption(new Option("md", "max-depth", true, "Maximum depth of inner folders to scan for photo files " +
                "default is infinity"));
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(83);
        try {
            CommandLine cl = parser.parse(options, args);
            dateFormat = cl.getOptionValue("date-format", dateFormat);
            maxDepth = Integer.parseInt(cl.getOptionValue("max-depth", "-1"));
            dryRun = cl.hasOption("dry-run");
            photoDirPaths = cl.getArgs();
            if (cl.hasOption("help") || photoDirPaths.length == 0) {
                formatter.printHelp(String.format("java -jar %s.jar [OPTION]... <PATH>...", PhotoRename.class.getSimpleName()), "Options:", options, "");
                System.exit(cl.hasOption("help") ? 0 : 1);
            }
        } catch (UnrecognizedOptionException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (ParseException e) {
            System.err.println(e.toString() + System.lineSeparator()
                    + StringUtils.join(e.getStackTrace(), System.lineSeparator()));
            System.exit(1);
        }
        sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(TimeZone.getTimeZone("GTM"));
        for (String photoDirPath : photoDirPaths) {
            if (!new File(photoDirPath).isDirectory()) {
                System.err.println(String.format("[%s] is not a directory. Exiting.", photoDirPath));
                System.exit(1);
            }
        }
    }
}
