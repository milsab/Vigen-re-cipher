package exercise.vigenere;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    private static final Logger LOG = Logger.getLogger(App.class.getName());
    private static final String CIPHER_CHAR_SET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz \t\n\r~!@#$%^&*()_+-=[]\\{}|;':\",./<>?";

    private char[] charSet;
    private HashMap<Character, Integer> map = new HashMap<>();

    private enum ACTIONS {
        ENCRYPT,
        DECRYPT,
        ENCRYPT_DIR,
        DECRYPT_DIR
    }

    public App() {

        charSet = CIPHER_CHAR_SET.toCharArray();

        /**
         * Map each character of "source character set" to its index in charSet
         * array
         */
        for (int index = 0; index < charSet.length; index++) {
            map.put(charSet[index], index);
        }
    }

    public static void main(String args[]) {
        if (args.length != 3) {
            System.out.println("Exact 3 parameters required - [action] [key] [target]");
            System.exit(1);
        }

        String action, key, target;
        action = args[0];
        key = args[1];
        target = args[2];

        App app = new App();

        if ("encrypt".equalsIgnoreCase(action)) {
            System.out.println("encrypt [" + key + "], [" + target + "]");

            String result = app.encrypt(key, target);
            System.out.println(result);

        } else if ("decrypt".equalsIgnoreCase(action)) {
            System.out.println("decrypt [" + key + "], [" + target + "]");

            String result = app.decrypt(key, target);
            System.out.println(result);

        } else if ("encryptDir".equalsIgnoreCase(action)) {
            System.out.println("encryptDir [" + key + "], [" + target + "]");
            
            String path = app.getResultPath(target, ACTIONS.ENCRYPT);
            app.dfsTraverse(key, target, path, ACTIONS.ENCRYPT_DIR);

        } else if ("decryptDir".equalsIgnoreCase(action)) {
            System.out.println("decryptDir [" + key + "], [" + target + "]");
                        
            String path = app.getResultPath(target, ACTIONS.DECRYPT);
            app.dfsTraverse(key, target, path, ACTIONS.DECRYPT_DIR);

        } else {
            System.out.println("action [" + action + "] not implemented");
        }

    }

    public String encrypt(String key, String target) {
        String result = "";

        int keyIndex = 0;
        for (int i = 0; i < target.length(); i++) {

            /**
             * Check if needs to rotate the mapping back to the first character
             * of the key
             */
            if (keyIndex == key.length()) {
                keyIndex = 0;
            }

            /**
             * If an input character is outside of the source character set, it
             * would be copied as-is to the encrypted output, and the mapping
             * does not move to the next one in the sequence.
             */
            if (!map.containsKey(target.charAt(i))) {
                result += target.charAt(i);
                continue;
            }

            /**
             * Find the current character's index of the key in the original
             * source character set
             */
            int position = map.get(key.charAt(keyIndex));
            char[] rotatedArr = rotate(charSet, charSet.length - position);
            result += rotatedArr[map.get(target.charAt(i))];
            keyIndex++;
        }

        return result;
    }

    public String decrypt(String key, String target) {
        String result = "";

        int keyIndex = 0;
        for (int i = 0; i < target.length(); i++) {
            if (keyIndex == key.length()) {
                keyIndex = 0;
            }

            if (!map.containsKey(target.charAt(i))) {
                result += target.charAt(i);
                continue;
            }

            int position = map.get(target.charAt(i)) - map.get(key.charAt(keyIndex));
            if (position < 0) {
                position = charSet.length + position;
            }
            result += charSet[position];
            keyIndex++;
        }
        return result;
    }

    /**
     * Determine the main path for the results
     *
     * @param target
     * @param action
     * @return
     */
    public String getResultPath(String target, ACTIONS action) {
        Path path = Paths.get(target);
        Path parentPath = path.getParent();
        String tail = "";
        if (action == ACTIONS.ENCRYPT) {
            tail = ".encrypted";
        } else if (action == ACTIONS.DECRYPT) {
            tail = ".decrypted";
        }
        String resultPath
                = (parentPath == null ? "" : parentPath.toString())
                + File.separator + path.getFileName() + tail;

        return resultPath;
    }

    /**
     * Traverse through all files and sub-folders by applying DFS approach
     *
     * @param key
     * @param path
     * @param resultPath
     * @param action
     */
    public void dfsTraverse(String key, String path, String resultPath, ACTIONS action) {

        /**
         * List all files and sub-folders
         */
        String[] files = (new File(path)).list();

        /**
         * Create the result folder
         */
        File file = new File(resultPath);
        file.mkdirs();

        for (String fileName : files) {
            String pathName = path + File.separator + fileName;
            String resultPathName = resultPath + File.separator + fileName;
            try {
                /**
                 * If there is any sub-folder in current folder we the same
                 * process in the sub-folder too by using DFS approach
                 */
                if ((new File(pathName)).isDirectory()) {
                    dfsTraverse(key, pathName, resultPathName, action);
                    continue;
                }

                /**
                 * Original Text
                 */
                String text
                        = new String(Files.readAllBytes(Paths.get(pathName)));

                /**
                 * Processed Text after doing Encryption/Decryption
                 */
                String processedText = null;
                if (action == ACTIONS.ENCRYPT_DIR) {
                    processedText = encrypt(key, text);
                } else if (action == ACTIONS.DECRYPT_DIR) {
                    processedText = decrypt(key, text);
                }

                /**
                 * write the processed text to the file
                 */
                File newFile = new File(resultPathName);
                FileWriter fw = new FileWriter(newFile);
                fw.write(processedText);
                fw.close();

            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Rotate an array to the right by k step
     *
     * @param arr
     * @param k rotate by k step
     * @return rotated array
     */
    public char[] rotate(final char[] arr, int k) {

        char[] rotatedArr = new char[arr.length];
        for (int i = 0; i < arr.length; i++) {
            rotatedArr[(i + k) % arr.length] = arr[i];
        }
        return rotatedArr;
    }
}
