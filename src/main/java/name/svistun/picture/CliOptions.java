package name.svistun.picture;

/*
 * MIT License
 *
 * Copyright (c) 2022 Aleksey Svistunov
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public final class CliOptions {
  private static final String DATE_FORMAT_NAME = "date-format";
  private static final String MAX_DEPTH_NAME = "max-depth";
  private static final String DRY_RUN_NAME = "dry-run";
  private static final String PICTURE_DIR_PATHS_NAME = "PICTURE_DIR_PATHS";
  private static final String HELP_NAME = "help";
  private static final String DATE_FORMAT_DEFAULT = "yyyyMMdd'T'HHmmss";
  private static final int CLI_LINE_LENGTH = 83;
  private Map<String, Object> parsedArgs;
  private Options options;

  public CliOptions(String[] args) throws ParseException {
    parseArgs(args);
    check();
  }
  
  public void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(CLI_LINE_LENGTH);
    formatter.printHelp(String.format(
        "java -jar PictureRename.jar [OPTION]... <PATH>..."),
        "Options:",
        options,
        "");
  }

  public String getDateFormat() {
    return (String) parsedArgs.get(DATE_FORMAT_NAME);
  }
  
  public int getMaxDepth() {
    return (int) parsedArgs.get(MAX_DEPTH_NAME);
  }
 
  public boolean isDryRun() {
    return (boolean) parsedArgs.get(DRY_RUN_NAME);
  }

  public String[] getPictureDirPaths() {
    return (String[]) parsedArgs.get(PICTURE_DIR_PATHS_NAME);
  }
  
  public boolean isHelp() {
    return (boolean) parsedArgs.get(HELP_NAME);
  }
  
  private void defineOptions() {
    options = new Options();
    options.addOption(new Option("df",
        DATE_FORMAT_NAME,
        true,
        String.format(
        "Java patterned date format which files to be renamed in "
        + "https://docs.oracle.com/javase/8/docs/api/index.html "
        + "default is \"%s\"", DATE_FORMAT_DEFAULT)));
    options.addOption(new Option("dr",
        DRY_RUN_NAME,
        false,
        "Just output how rename will occur"));
    options.addOption("h",
        HELP_NAME,
        false,
        "Print help message");
    options.addOption(new Option("md",
        MAX_DEPTH_NAME,
        true,
        "Maximum depth of inner folders to scan for picture files "
        + "default is infinity"));
  }
  
  private void parseArgs(String[] args) throws ParseException {
    defineOptions();
    this.parsedArgs = new HashMap<>();
    CommandLineParser parser = new DefaultParser();
    CommandLine cl = parser.parse(options, args);
    this.parsedArgs.put(DATE_FORMAT_NAME, cl.getOptionValue(DATE_FORMAT_NAME,
        DATE_FORMAT_DEFAULT));
    this.parsedArgs.put(MAX_DEPTH_NAME, Integer.parseInt(cl.getOptionValue(MAX_DEPTH_NAME,
        "-1")));
    this.parsedArgs.put(DRY_RUN_NAME, cl.hasOption(DRY_RUN_NAME));
    this.parsedArgs.put(PICTURE_DIR_PATHS_NAME, cl.getArgs());
    this.parsedArgs.put(HELP_NAME, cl.hasOption(HELP_NAME));
  }
  
  private void check() throws ParseException {
    if (getPictureDirPaths().length == 0) {
      printHelp();
      throw new ParseException("Define path(s) where pictures have to be processed.");
    }
    for (String pictureDirPath : getPictureDirPaths()) {
      if (! new File(pictureDirPath).isDirectory()) {
        throw new ParseException(String.format(
            "[%s] is not a directory. Exiting.",
            pictureDirPath));
      }
    }
  }
}
