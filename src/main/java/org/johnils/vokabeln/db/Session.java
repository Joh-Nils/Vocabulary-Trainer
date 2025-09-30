package org.johnils.vokabeln.db;

import java.util.Random;

public class Session {
    private static final Random random = new Random();

    public final String language;
    public Vocab[] vocabs;
    private int count = 0;
    private int right = 0;
    private Vocab lastVocab;


    public Session(String language) {
        this.language = language;
    }

    public void increaseCount(boolean right) {
        count++;
        this.right += right ? 1 : 0;
    }

    public int count() {
        return this.count;
    }

    public Vocab getVocab() {
        lastVocab = vocabs[random.nextInt(vocabs.length)];

        return lastVocab;
    }

}
