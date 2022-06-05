# PhotoRename
A while after I collected a lot of images which could not be systematized in any good way and were stored in a single directory, I encountered with a problem of repeating of file names of the images.  
  
For the purpose this program was created.  
It can rename pictures, photos and videos, according to their shooting time.  
The program is able to process:
- NEF images. Image setting files stored in NKSC_PARAM folder are processed along with the image file;
- MOV videos;
- MP4 videos.
Just point to path(s) to the pictures and a directory depth to search, if needed. You may also override template to rename photos.
<pre><code>usage: java -jar PhotoRename.jar [OPTION]... &lt;PATH>...
Options:
 -df,--date-format &lt;arg>   Java patterned date format which files to be renamed in
                           https://docs.oracle.com/javase/8/docs/api/index.html
                           default is "yyyyMMdd'at'HHmm''ss"
 -dr,--dry-run             Just output how rename will occur
 -h,--help                 Print help message
 -md,--max-depth &lt;arg>     Maximum depth of inner folders to scan for photo files
                           default is infinity</code></pre>
