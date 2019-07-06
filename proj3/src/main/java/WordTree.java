import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class WordTree {
    protected HashMap<String, LinkedList<String>> wordNames;
    WordTree() {
        this.wordNames = new HashMap<>();
    }

    public void addWord(String location) {
        String letters = "";
        for (int i = 0; i < location.length(); i += 1) {
            letters += location.charAt(i);
            if (this.wordNames.containsKey(letters)) {
                LinkedList<String> words = this.wordNames.get(letters);
                words.addFirst(location);
                Collections.sort(words);
            } else {
                LinkedList<String> startsWith = new LinkedList<>();
                startsWith.addFirst(location);
                this.wordNames.put(letters, startsWith);
            }
        }
    }

}
