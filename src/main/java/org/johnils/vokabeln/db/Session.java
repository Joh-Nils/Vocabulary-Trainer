package org.johnils.vokabeln.db;

import java.util.Random;

public class Session {
    private static final Random random = new Random();

    public final String language;
    public Vocab[] vocabs;
    private int count = 0;
    private int right = 0;
    private Vocab lastVocab;
    private boolean lang;


    public Session(String language) {
        this.language = language;
    }

    private void increaseCount(boolean right) {
        count++;
        this.right += right ? 1 : 0;
    }

    public int count() {
        return this.count;
    }

    public Vocab getVocab() {
        lastVocab = vocabs[random.nextInt(vocabs.length)];
        lang = random.nextBoolean();

        return new Vocab(lang ? lastVocab.word() : lastVocab.translation(), "That's for you to find out, silly");
    }
    public Vocab getLastVocab() {
        return new Vocab(lang ? lastVocab.word() : lastVocab.translation(), lang ? lastVocab.translation() : lastVocab.word());
    }
    public boolean correct(String translation) {
        boolean right = (lang ? lastVocab.translation() : lastVocab.word()).equals(translation);

        increaseCount(right);

        return right;
    }

    public SessionData getData() {
        return new SessionData(right, count - right);
    }
}
