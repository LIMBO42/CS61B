package capers;

import java.io.*;

import static capers.Utils.*;

/** A repository for Capers 
 * @author TODO
 * The structure of a Capers Repository is as follows:
 *
 * .capers/ -- top level folder for all persistent data in your lab12 folder
 *    - dogs/ -- folder containing all of the persistent data for dogs
 *    - story -- file containing the current story
 *
 * TODO: change the above structure if you do something different.
 */
public class CapersRepository {
    /** Current Working Directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Main metadata folder. */
    static final File CAPERS_FOLDER = Utils.join(CWD,".capers/"); // TODO Hint: look at the `join`
                                            //      function in Utils

    /**
     * Does required filesystem operations to allow for persistence.
     * (creates any necessary folders or files)
     * Remember: recommended structure (you do not have to follow):
     *
     * .capers/ -- top level folder for all persistent data in your lab12 folder
     *    - dogs/ -- folder containing all of the persistent data for dogs
     *    - story -- file containing the current story
     */
    public static void setupPersistence() {
        // TODO
        CAPERS_FOLDER.mkdir();
        File dogsFile = Utils.join(CAPERS_FOLDER,"dogs/");
        dogsFile.mkdir();
        File storyFile = Utils.join(CAPERS_FOLDER,"story");
        //storyFile.createNewFile();
    }

    /**
     * Appends the first non-command argument in args
     * to a file called `story` in the .capers directory.
     * @param text String of the text to be appended to the story
     */
    public static void writeStory(String text){
        // TODO
        File storyFile = Utils.join(CAPERS_FOLDER,"story");
        if(!storyFile.exists()) {
            try {
                storyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter fileWritter = new FileWriter(storyFile.getAbsoluteFile(),true);
            fileWritter.append(text+"\n");
            fileWritter.close();
            //System.out.println(text);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //读文件
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new FileInputStream(storyFile), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String lineTxt = null;
            while ((lineTxt = br.readLine()) != null) {
                System.out.println(lineTxt);
            }
            br.close();
            isr.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates and persistently saves a dog using the first
     * three non-command arguments of args (name, breed, age).
     * Also prints out the dog's information using toString().
     */
    public static void makeDog(String name, String breed, int age) {
        // TODO
        //System.out.println("makeDog");
        Dog aDog = new Dog(name, breed, age);
        System.out.println(aDog.toString());
        aDog.saveDog();
    }

    /**
     * Advances a dog's age persistently and prints out a celebratory message.
     * Also prints out the dog's information using toString().
     * Chooses dog to advance based on the first non-command argument of args.
     * @param name String name of the Dog whose birthday we're celebrating.
     */
    public static void celebrateBirthday(String name) {
        // TODO
        File dogFile = Utils.join(CAPERS_FOLDER,"dogs/"+name);
        Dog aDog = Utils.readObject(dogFile,Dog.class);
        aDog.haveBirthday();
        //System.out.println(aDog.toString());
        Utils.writeObject(dogFile,aDog);
    }
}
