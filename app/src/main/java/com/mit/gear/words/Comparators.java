package com.mit.gear.words;

import java.util.Comparator;

/**
 * Created by NuhaKhayat on 7/8/2016 AD.
 * This class is used for comparing words, definitions, clicks and passes
 * and sorting them column comparator is set to the appropriate method
 */
public final class Comparators {

    public Comparators() {
    }

    public static Comparator<Word> SetWordComparator() {
        return new WordComparator();
    }

    public static Comparator<Word> SetPassesComparator() {
        return new PassesComparator();
    }

    public static Comparator<Word> SetLemmaComparator() {
        return new LemmaComparator();
    }

    public static Comparator<Word> SetClicksComparator() {
        return new ClicksComparator();
    }

	public static Comparator<Word> SetTimesComparator() {
		return new TimeComparator();
	}

	public static Comparator<Word> SetWordComparatorReverse() {
		return new WordComparatorReverse();
	}

	public static Comparator<Word> SetPassesComparatorReverse() {
		return new PassesComparatorReverse();
	}

	public static Comparator<Word> SetLemmaComparatorReverse() {
		return new LemmaComparatorReverse();
	}

	public static Comparator<Word> SetClicksComparatorReverse() {
		return new ClicksComparatorReverse();
	}

	public static Comparator<Word> SetTimesComparatorReverse() {
		return new TimeComparatorReverse();
	}

    /*
    * Inner classes to override compare method
    */

    private static class WordComparator implements Comparator<Word> {
        @Override
        public int compare(final Word word1, final Word word2) {
            return word1.getWord().compareToIgnoreCase(word2.getWord());

        }
    }

    private static class PassesComparator implements Comparator<Word> {
        @Override
        public int compare(final Word word1, final Word word2) {
            if (word1.totalWordPasses() > word2.totalWordPasses()) return -1;
            if (word1.totalWordPasses() < word2.totalWordPasses()) return 1;
            return 0;
        }
    }

    private static class LemmaComparator implements Comparator<Word> {
        @Override
        public int compare(final Word word1, final Word word2) {
            return word1.getLemma().compareToIgnoreCase(word2.getLemma());
        }
    }

    private static class ClicksComparator implements Comparator<Word> {
        @Override
        public int compare(final Word word1, final Word word2) {
            if (word1.totalWordClicks() > word2.totalWordClicks()) return -1;
            if (word1.totalWordClicks() < word2.totalWordClicks()) return 1;
            return 0;
        }
    }

    private static class TimeComparator implements Comparator<Word> {
        @Override
        public int compare(final Word word1, final Word word2) {
            if (word1.getClickTime() > word2.getClickTime()) return -1;
            if (word1.getClickTime() < word2.getClickTime()) return 1;
            return 0;
        }
    }

	private static class WordComparatorReverse implements Comparator<Word> {
		@Override
		public int compare(final Word word1, final Word word2) {
			return word2.getWord().compareToIgnoreCase(word1.getWord());

		}
	}

	private static class PassesComparatorReverse implements Comparator<Word> {
		@Override
		public int compare(final Word word1, final Word word2) {
			if (word2.totalWordPasses() > word1.totalWordPasses()) return -1;
			if (word2.totalWordPasses() < word1.totalWordPasses()) return 1;
			return 0;
		}
	}

	private static class LemmaComparatorReverse implements Comparator<Word> {
		@Override
		public int compare(final Word word1, final Word word2) {
			return word2.getLemma().compareToIgnoreCase(word1.getLemma());
		}
	}

	private static class ClicksComparatorReverse implements Comparator<Word> {
		@Override
		public int compare(final Word word1, final Word word2) {
			if (word2.totalWordClicks() > word1.totalWordClicks()) return -1;
			if (word2.totalWordClicks() < word1.totalWordClicks()) return 1;
			return 0;
		}
	}

	private static class TimeComparatorReverse implements Comparator<Word> {
		@Override
		public int compare(final Word word1, final Word word2) {
			if (word2.getClickTime() > word1.getClickTime()) return -1;
			if (word2.getClickTime() < word1.getClickTime()) return 1;
			return 0;
		}
	}
}
