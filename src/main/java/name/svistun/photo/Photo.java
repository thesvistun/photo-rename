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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Photo {
    private Date dateTaken;
    private File photoFile, paramsFile;
    private Pattern patternParamsFile;
    private static Pattern patternPhotoFile;
    private static Map<String, String> paramFileExtToFolderNameMap;
    static {
        paramFileExtToFolderNameMap = new HashMap<>();
        paramFileExtToFolderNameMap.put("nksc", "NKSC_PARAM");
        patternPhotoFile = Pattern.compile("(.+)(\\.(.+))");
    }

    Photo(File photoFile) throws IOException, ImageProcessingException, NotImageFileException {
        this.photoFile = photoFile;
        init();
    }

    boolean rename(boolean dryRun, SimpleDateFormat sdf) throws IOException, ImageProcessingException {

        return  check(photoFile, patternPhotoFile, sdf) && (null == paramsFile || check(paramsFile, patternParamsFile, sdf)) &&
                    rename(photoFile, patternPhotoFile, dryRun, sdf) && (null == paramsFile || rename(paramsFile, patternParamsFile, dryRun, sdf));
    }

    private void init() throws IOException, ImageProcessingException, NotImageFileException {
        Matcher matcherPhoto = patternPhotoFile.matcher(photoFile.getName());
        if (matcherPhoto.find()) {
            String photoFileExt = matcherPhoto.group(3);
            if (paramFileExtToFolderNameMap.containsKey(photoFileExt.toLowerCase())) {
                throw new NotImageFileException("recognized as a settings file.");
            }
            for (String paramFileExt : paramFileExtToFolderNameMap.keySet()) {
                File paramFileFolder = new File(photoFile.getParent() + File.separator + paramFileExtToFolderNameMap.get(paramFileExt));
                if (paramFileFolder.exists()) {
                    File[] paramsFileFolderFiles = paramFileFolder.listFiles();
                    if (null == paramsFileFolderFiles) {
                        throw new IOException(String.format("Directory %s returned null at getting list of its files.", paramFileFolder.getName()));
                    } else {
                        Pattern _patternParamsFile = Pattern.compile(String.format("(.+?)((\\.(%s))?\\.(%s|%s))", photoFileExt, paramFileExt, paramFileExt.toUpperCase()));
                        for (File _paramsFile : paramsFileFolderFiles) {
                            Matcher matcherParams = _patternParamsFile.matcher(_paramsFile.getName());
                            if (matcherParams.matches()) {
                                if (matcherParams.group(1).equals(matcherPhoto.group(1))) {
                                    paramsFile = _paramsFile;
                                    patternParamsFile = _patternParamsFile;
                                    break;
                                }
                            }
                        }
                        if (paramsFile != null) {
                            break;
                        }
                    }
                }
            }
        }
        Metadata metadata = ImageMetadataReader.readMetadata(photoFile);
        // obtain the Exif SubIFD directory
        for (ExifSubIFDDirectory directory : metadata.getDirectoriesOfType(ExifSubIFDDirectory.class)) {
            dateTaken = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (dateTaken != null) {
                break;
            }
        }
        if (null == dateTaken) {
            throw new ImageProcessingException("could not find date taken in EXIF");
        }
    }

    private boolean check(File file, Pattern pattern, SimpleDateFormat sdf) {
        Matcher matcher = pattern.matcher(file.getName());
        if (matcher.matches()) {
            File newFile = new File(file.getParent() + File.separator + sdf.format(dateTaken) + matcher.group(2));
            if (file.getName().equals(newFile.getName())) {
                System.out.println(String.format("File [%s] already has properly name.", file.getAbsoluteFile()));
                return false;
            }
            if (newFile.exists()) {
                System.err.println(String.format("File [%s] already exists.", file.getAbsoluteFile()));
                return false;
            }
            if (! file.canWrite() || ! newFile.getParentFile().canWrite()) {
                System.err.println(String.format("Access problems. File [%s] can not be renamed.", file.getAbsoluteFile()));
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean rename(File file, Pattern pattern, boolean dryRun, SimpleDateFormat sdf) {
        Matcher matcher = pattern.matcher(file.getName());
        if (matcher.matches()) {
            File newFile = new File(file.getParent() + File.separator + sdf.format(dateTaken) + matcher.group(2));
            if (! dryRun) {
                if (! file.renameTo(newFile)) {
                    System.err.println(String.format("%s -X-> %s", file.getAbsoluteFile(), newFile.getName()));
                    return false;
                }
            }
            System.out.println(String.format("%s --> %s", file.getAbsoluteFile(), newFile.getName()));
        }
        return true;
    }

    @Override
    public String toString() {
        return "Photo " + photoFile.getAbsolutePath() +
                (null == paramsFile ? "" : String.format(" (%s)", paramsFile.getAbsolutePath()));
    }
}
