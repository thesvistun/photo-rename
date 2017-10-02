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
    private File photoFile, paramsFile;
    private Pattern patternPhotoFile, patternParamsFile;
    private static Map<String, String> paramFileFolderNameMap, paramFileExtMap;
    static {
        paramFileExtMap = new HashMap<>();
        paramFileExtMap.put("nef", "nksc");
        paramFileFolderNameMap = new HashMap<>();
        paramFileFolderNameMap.put("nef", "NKSC_PARAM");
    }

    Photo(File photoFile) throws IOException {
        this.photoFile = photoFile;
        String photoFileExt = photoFile.getName().substring(photoFile.getName().lastIndexOf(".") + 1);
        patternPhotoFile = Pattern.compile(String.format("(.+?)(\\.(%s))", photoFileExt));
        patternParamsFile = Pattern.compile(String.format("(.+?)((\\.(%s))?\\.(%s|%s))", photoFileExt, paramFileExtMap.get(photoFileExt.toLowerCase()), null == paramFileExtMap.get(photoFileExt.toLowerCase()) ? null : paramFileExtMap.get(photoFileExt.toLowerCase()).toUpperCase()));
        File paramFileFolder = new File(photoFile.getParent() + File.separator + paramFileFolderNameMap.get(photoFileExt.toLowerCase()));
        if (paramFileFolder.exists()) {
            File[] paramFileFolderFiles = paramFileFolder.listFiles();
            if (null == paramFileFolderFiles) {
                throw new IOException(String.format("Dir %s returned null at getting list of its files.", paramFileFolder.getName()));
            } else {
                for (File _paramsFile : paramFileFolderFiles) {
                    Matcher matcherPhoto = patternPhotoFile.matcher(photoFile.getName());
                    Matcher matcherParams = patternParamsFile.matcher(_paramsFile.getName());
                    if (matcherParams.matches() && matcherPhoto.matches()) {
                        if (matcherParams.group(1).equals(matcherPhoto.group(1))) {
                            paramsFile = _paramsFile;
                            break;
                        }
                    }
                }
            }
        }
    }

    boolean rename(boolean dryRun, SimpleDateFormat sdf) throws IOException, ImageProcessingException {
        Metadata metadata = ImageMetadataReader.readMetadata(photoFile);
        // obtain the Exif SubIFD directory
        Date date = null;
        for (ExifSubIFDDirectory directory : metadata.getDirectoriesOfType(ExifSubIFDDirectory.class)) {
            date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (date != null) {
                break;
            }
        }
        if (null == date) {
            System.err.println(String.format("File [%s] does not have EXIF of origin date", photoFile.getAbsoluteFile()));
            return false;
        }
        Matcher matcherPhoto = patternPhotoFile.matcher(photoFile.getName());
        if (matcherPhoto.matches()) {
            File newPhotoFile = new File(photoFile.getParent() + File.separator + sdf.format(date) + matcherPhoto.group(2));
            if (photoFile.getName().equals(newPhotoFile.getName())) {
                System.out.println(String.format("File [%s] already has properly name.", photoFile.getAbsoluteFile()));
                return false;
            }
            if (newPhotoFile.exists()) {
                System.err.println(String.format("Photo file [%s] already exists.", photoFile.getAbsoluteFile()));
                return false;
            }
            if (! dryRun) {
                if (! photoFile.renameTo(newPhotoFile)) {
                    System.err.println(String.format("Photo file [%s] was not renamed.", photoFile.getAbsoluteFile()));
                    return false;
                }
            } else if (! photoFile.canWrite() || ! newPhotoFile.getParentFile().canWrite()) {
                System.err.println(String.format("Photo file [%s] can not be renamed.", photoFile.getAbsoluteFile()));
                return false;
            }
            System.out.println(String.format("Photo %s -> %s", photoFile.getAbsoluteFile(), newPhotoFile.getName()));
        }
        if (paramsFile != null) {
            Matcher matcherParams = patternParamsFile.matcher(paramsFile.getName());
            if (matcherParams.matches()) {
                File newParamsFile = new File(paramsFile.getParent() + File.separator + sdf.format(date) + matcherParams.group(2));
                if (newParamsFile.exists()) {
                    System.err.println(String.format("Param file [%s] already exists.", paramsFile.getAbsoluteFile()));
                    return false;
                }
                if (! dryRun) {
                    if (! paramsFile.renameTo(newParamsFile)) {
                        System.err.println(String.format("Params file [%s] was not renamed.", paramsFile.getAbsoluteFile()));
                        return false;
                    }
                } else if (! paramsFile.canWrite() || ! newParamsFile.getParentFile().canWrite()) {
                    System.err.println(String.format("Params file [%s] can not be renamed.", paramsFile.getAbsoluteFile()));
                    return false;
                }
                System.out.println(String.format("Params %s -> %s", paramsFile.getAbsoluteFile(), newParamsFile.getName()));
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Photo " + photoFile.getAbsolutePath() +
                (null == paramsFile ? "" : String.format(" (%s)", paramsFile.getAbsolutePath()));
    }
}
