import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import static java.nio.file.FileVisitResult.CONTINUE;

public class SearchFolderAndFileThenReplaceBytes {

    public static class Finder
            extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;
        private static int matchingCounter = 0;
        final static private List<String> directories = new ArrayList<>();

        Finder(String firstDirectorySelected) {
            matcher = FileSystems.getDefault()
                    .getPathMatcher("glob:" + firstDirectorySelected);
        }

        // Compares the glob firstDirectorySelected against
        // the file or directory name.
        void find(Path file) {
            Path name = file.getFileName();
            if (name != null && matcher.matches(name)) {
                matchingCounter++;
                directories.add(file.toString());
                System.out.println(matchingCounter + ". " + file.toString());
            }
        }

        // displays information on the number of corresponding subfolders
        void done() {
            if (matchingCounter > 1) {
                System.out.println(matchingCounter + " = Number of matching directories," +
                        " Copy and paste one line from the above list without number, period and spaces");
            } else if (matchingCounter == 1) {
                System.out.println("One matching subdirectory was found");
            } else {
                 System.err.println("The directory you are looking for does not exist. Enter \"y\" to continue or any other character(s) to exit");
                 boolean continueOrNot = continueYesOrNo(SCANNER.nextLine());
                 if (!continueOrNot) {
                     System.out.println("I close the program");
                     System.exit(0);
                 }
            }
        }

        // Invoke the firstDirectorySelected matching
        // method on each file.
        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs) {
            find(file);
            return CONTINUE;
        }

        // Invoke the firstDirectorySelected matching
        // method on each directory.
        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                                                 BasicFileAttributes attrs) {
            find(dir);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file,
                                               IOException exc) {
            System.err.println(exc.toString());
            return CONTINUE;
        }
    }

    //fields in the Main class
    private final static File STARTING_FILE = new File("C:\\Users\\48665\\OneDrive\\Pulpit\\homework");
    private final static Path STARTING_PATH = Paths.get(STARTING_FILE.toString());
    private final static Scanner SCANNER = new Scanner(System.in);
    private static String selectedFolder = null;
    public static List<File> matchingFilesByFolderName = new ArrayList<>();
    public static String fileExtension = ".";
    public static List<Byte> searchedBytes = new ArrayList<>();
    // both of the following lists are related, the indexes correspond to the file and its list of offsets,
    // telling about the locations of the bytes searched
    public static List<File> matchingFilesByMatchingBytes = new ArrayList<>();
    public static List<List<Integer>> offsetsForMatchingByteSequences = new ArrayList<>();
    public static List<Byte> modifiedBytes = new ArrayList<>();

    // the method checks the status of the starting path to see if it exists, is a directory, and displays the status
    public static void showStatusOfStartingPath(Path path, File file) {
        File[] files = file.listFiles();

        if (!Files.exists(path) && !Files.isDirectory(path)) {
            System.err.println("\"" + path + "\" <- The path does not exist or is not a folder \n I finish work...");
        } else if (STARTING_FILE.exists() && STARTING_FILE.isDirectory()) {
            if ((files != null ? files.length : 0) == 0) {
                System.err.println("\"" + STARTING_FILE.toString() + "\"" + " <- Main folder is empty" +
                        "\nI finish work...");
                System.exit(0);
            } else {
                int count = 0;
                for (File fileIterator : files) {
                    if (fileIterator.isDirectory()) {
                        count++;
                    }
                }
                if (count == 0) {
                    System.err.println("\"" + STARTING_FILE.toString() + "\"" + " <- Main folder has no subfolders" +
                            "\n I finish work...");
                    System.exit(0);
                } else {
                    System.out.println("\"" + STARTING_FILE.toString() + "\"" + " <- Main folder (subfolders)");
                }
            }
        }
    }

    /* the method gets data from the user from the console, checks if the entered string is not too short / too long
     and if it does not contain illegal characters */
    private static void getNameOfFolder() {
        String input = SCANNER.nextLine();

        while (true) {
            if (input.length() == 0) {
                System.err.println("Nothing was entered. Try again:");
                input = SCANNER.nextLine();
            }
            else if (input.length() + STARTING_FILE.toString().length() > 260) {
                System.err.println("The length of the path, including the directory name, cannot exceed 260 characters. Try again:");
                input = SCANNER.nextLine();
            }
            else {
                String[] forbiddenChar = {"<", ">", "?", "/", "\\", "*", ":", "\"", "|"};
                boolean isInputContainForbiddenChar = false;
                for (String string : forbiddenChar) {
                    if (input.contains(string)) {
                        isInputContainForbiddenChar = true;
                        System.err.println("The entered string contains an illegal character. Try again:");
                        input = SCANNER.nextLine();
                    }
                }
                if (!isInputContainForbiddenChar) {
                    selectedFolder = input;
                    break;
                }
            }
        }
    }

    // method checking if copied and pasted path (String) matches any of the list
    public static Path searchPathInDirectoryTree() {
        String inputPath = SCANNER.nextLine().trim();
        File finalDirectory = null;
        Path pathOfDirectory = null;
        int counterFolder = 0;

        while (finalDirectory == null) {
            for (String oneOfTheSubFolder : Finder.directories) {
                if (oneOfTheSubFolder.equals(inputPath)) {
                    finalDirectory = new File(inputPath);
                    pathOfDirectory = finalDirectory.toPath();
                } else {
                    counterFolder++;
                    if (counterFolder == Finder.directories.size()) {
                        System.err.println("Incorrectly entered path, make sure you pasted the path line without" +
                                " the line number and period");
                        counterFolder = 0;
                        inputPath = SCANNER.nextLine().trim();
                    }
                }
            }
        }
        return pathOfDirectory;
    }

    // the method checks if the given extension (argument) does not contain illegal characters
    public static void validateFileExtension() {
        String extension = SCANNER.nextLine().trim();
        boolean isNotOK = true;
        while (isNotOK) {
          //  String extension = input.trim();
            char[] argumentAsCharacters = extension.toCharArray();
            char[] forbiddenCharacters = {'*', '/', '?', '>', '<', '\"', ':', '\\', '|', '.'};

            int countingForbiddenCharacters = 0;
            if (extension.length() == 0) {
                System.err.println("Nothing was entered, try again: ");
                break;
            }
            else if (argumentAsCharacters.length > 0) {
                for (char character : argumentAsCharacters) {
                    for (char forbidden : forbiddenCharacters) {
                        if (character == forbidden) {
                            countingForbiddenCharacters++;
                        }
                    }
                }
                if (countingForbiddenCharacters > 0) {
                    System.err.println("Entered value contains illegal character(s), try again: ");
                    break;
                }
                else {
                    isNotOK = false;
                    fileExtension += extension;
                }
            }
        }
    }

    // the method returns a list of files in the selected folder and subfolders that match the entered extension
    public static List<File> findAllMatchingFilesInDirectoryTree(File[] file, String type) {

        for (File singleFile : file) {
            if (singleFile.isFile() && singleFile.toString().endsWith(type)) {
                matchingFilesByFolderName.add(singleFile);
            } else if (singleFile.isDirectory()) {
                // TODO hint intelliJ "Object.requireNonNull(...)"
                findAllMatchingFilesInDirectoryTree(Objects.requireNonNull(singleFile.listFiles()), type);
            }
        }
        return matchingFilesByFolderName;
    }

    // method displaying list of found files in folder and subfolders
    public static void printMatchingFiles(List<File> files) {
        for (File file : files) {
            System.out.println(file.toString());
        }

        if (matchingFilesByFolderName.size() != 0) {
            System.out.println("The number of files with the given extension was found: " + matchingFilesByFolderName.size());
        } else {
            System.err.println("No files with the given extension were found, want to continue? enter \"y\"" +
                    " if you want to continue or any other character(s) if you want to quit");
            boolean yesNo = continueYesOrNo(SCANNER.nextLine());
            if (!yesNo) {
                System.out.println("I close the program");
                System.exit(0);
            }
        }
    }

    // method validates entered bytes and adds them to the list
    public static void validateAndAddBytes(List<Byte> bytes) {
        String input = SCANNER.nextLine();
        if (input.length() == 0 || input.trim().length() == 0) {
            System.err.println("nothing was entered, try again or if you want to quit the program enter \"x\"");
        }
        else if (input.equals("x")) {
            System.out.println("I close the program");
            SCANNER.close();
            System.exit(0);
        }
        else {
            String[] charactersFromInput = input.split(" ");
            for (String string : charactersFromInput) {
                try {
                    bytes.add(Byte.parseByte(string));
                } catch (NumberFormatException e) {
                    System.err.println("The value entered does not represent bytes. Please try again or exit by entering \"x\"");
                    bytes.clear();
                    break;
                }
            }
        }
    }

    // method print all files contains searched bytes
    public static void showFilesContainingSearchedBytes (List<File> matchingFiles) {
        if (matchingFiles.size() == 0) {
            System.err.println("no files with the given extension and given bytes were found");
            System.exit(0);
        }
        else {
            System.out.println("These files contain the searched bytes:");
            for (File f : matchingFiles) {
                System.out.println(f.toString());
            }
        }
    }

    // method
    public static void replaceBytesInFiles() throws Exception {
        for(int fileNo = 0; fileNo < matchingFilesByMatchingBytes.size(); fileNo++) {
            int countingSequence = 0;
            List<Byte> modifiedFile = new ArrayList<>();
            byte[] conversionFile = Files.readAllBytes (matchingFilesByMatchingBytes.get(fileNo).toPath());
            Byte[] conversionFile2 = new Byte[conversionFile.length];
            Byte[] resultToSaveInFile;
            byte[] result;

            for (int x = 0; x < conversionFile.length; x++) {
                conversionFile2[x] = conversionFile[x];
            }

            for (int i = 0; i < conversionFile2.length;) {
                if (i == offsetsForMatchingByteSequences.get(fileNo).get(countingSequence)) {
                    modifiedFile.addAll(modifiedBytes);
                    i += searchedBytes.size();
                    if (countingSequence < offsetsForMatchingByteSequences.get(fileNo).size() - 1) {
                        countingSequence++;
                    }
                }
                else {
                    modifiedFile.add(conversionFile2[i]);
                    i++;
                }
            }

            new PrintWriter(matchingFilesByMatchingBytes.get(fileNo)).close();
            resultToSaveInFile = modifiedFile.toArray(new Byte[0]);
            result = new byte[resultToSaveInFile.length];
            int y = 0;

            for (Byte b : resultToSaveInFile) {
                result[y++] = b;
            }

            try (FileOutputStream fos = new FileOutputStream(matchingFilesByMatchingBytes.get(fileNo))) {
                fos.write(result);
                //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
            }

            System.out.println("Byte swapping completed successfully for: " + matchingFilesByMatchingBytes.get(fileNo).toString());
        }
    }

    public static List<File> validateFilesByMatchingBytes (List<File> allMatchingFilesByFolderName) {
        List<File> matchingFiles = new ArrayList<>();
        // this loop iterates through matching files
        for (File file : allMatchingFilesByFolderName) {
            // this table is designed to store the contents of a file as single bytes
            byte[] convertFile = null;
            try {
                convertFile = Files.readAllBytes(file.toPath());
            }
            catch (IOException e) {
                //
            }
            // this array and the loop below it are used to wrap primitive bytes from the above byte array into Byte Objects
            assert convertFile != null;
            Byte[] convertFromPrimitive = new Byte[convertFile.length];
            int numberOfByte = 0;
            for (byte b : convertFile) {
                convertFromPrimitive[numberOfByte++] = b;
            }

            List<Integer> offsetsOfMatchingSeries = new ArrayList<>();
            int countCorrectSeries = 0;
            for (int i = 0; i < convertFromPrimitive.length; i++) {
                if (searchedBytes.size() == 1 && convertFromPrimitive[i].equals(searchedBytes.get(0))) {
                    offsetsOfMatchingSeries.add(i);
                }
                else if (searchedBytes.size() != 0 && convertFromPrimitive[i].equals(searchedBytes.get(countCorrectSeries))) {
                    countCorrectSeries++;
                    if (countCorrectSeries == searchedBytes.size()) {
                        offsetsOfMatchingSeries.add(i - searchedBytes.size() + 1);
                        countCorrectSeries = 0;
                    }
                }
                else if (!convertFromPrimitive[i].equals(searchedBytes.get(countCorrectSeries)) && convertFromPrimitive[i].equals(searchedBytes.get(0))) {
                    countCorrectSeries = 1;
                }
                else {
                    countCorrectSeries = 0;
                }
            }
            if (offsetsOfMatchingSeries.size() > 0) {
                matchingFiles.add(file);
                offsetsForMatchingByteSequences.add(offsetsOfMatchingSeries);
            }
        }
        return matchingFiles;
    }

    public static boolean continueYesOrNo(String input) {
        boolean isTrue = false;
        if (input.equalsIgnoreCase("y")) {
            isTrue = true;
            System.out.println("Enter the searched data again:");
        }
        return isTrue;
    }

    public static void main(String[] args) throws Exception {
        showStatusOfStartingPath(STARTING_PATH, STARTING_FILE);
        System.out.println("Enter the name of the directory you are looking for:");
        getNameOfFolder();
        Finder finder = new Finder(selectedFolder);
        Files.walkFileTree(STARTING_PATH, finder);
        finder.done();
        while (Finder.matchingCounter == 0) {
            getNameOfFolder();
            finder = new Finder(selectedFolder);
            Files.walkFileTree(STARTING_PATH,finder);
            finder.done();
        }
        Path chosenDirectory = Paths.get(Finder.directories.get(0));
        if ((Finder.matchingCounter != 1)) {
            chosenDirectory = searchPathInDirectoryTree();
        }

        System.out.println("Enter an extension type to find files with that extension (enter e.g. \"png\")");
        validateFileExtension();
        while (fileExtension.equals(".")) {
            validateFileExtension();
        }
        File chosenFile = chosenDirectory.toFile();
        File[] files = chosenFile.listFiles();
        assert files != null;
        List<File> listOfMatcherFiles = findAllMatchingFilesInDirectoryTree(files, fileExtension);
        printMatchingFiles(listOfMatcherFiles);
        while (listOfMatcherFiles.size() == 0) {
            fileExtension = ".";
            validateFileExtension();
            while (fileExtension.equals(".")) {
                validateFileExtension();
            }
            listOfMatcherFiles = findAllMatchingFilesInDirectoryTree(files, fileExtension);
            printMatchingFiles(listOfMatcherFiles);
        }

        System.out.println("Enter the bytes you are looking for, separated by single spaces then press enter:");
        validateAndAddBytes(searchedBytes);
        while (searchedBytes.size() == 0) {
            validateAndAddBytes(searchedBytes);
        }
        matchingFilesByMatchingBytes = validateFilesByMatchingBytes(matchingFilesByFolderName);
        showFilesContainingSearchedBytes(matchingFilesByMatchingBytes);

        System.out.println("Enter the bytes you want to replace the previously searched for bytes with:");
        validateAndAddBytes(modifiedBytes);
        while (modifiedBytes.size() == 0) {
            validateAndAddBytes(modifiedBytes);
        }

        SCANNER.close();
        replaceBytesInFiles();
        System.out.println("You can now close this window");
    }
}
