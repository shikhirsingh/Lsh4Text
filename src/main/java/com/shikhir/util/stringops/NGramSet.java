package com.shikhir.util.stringops;
import java.util.Iterator;
import java.util.LinkedHashSet;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.util.StringList;
import opennlp.tools.util.StringUtil;

/**
 * The {@link NGramSet} can be used to crate ngrams and character ngrams.
 *
 * @see StringList
 */
public class NGramSet implements Iterable<StringList> {

  protected static final String COUNT = "count";

  private LinkedHashSet<StringList> mNGrams = new LinkedHashSet<StringList>();

  /**
   * Initializes an empty instance.
   */
  public NGramSet() {
  }


  /**
   * Adds one NGram, if it already exists the count increase by one.
   *
   * @param ngram
   */
  public void add(StringList ngram) {
      mNGrams.add(ngram);
  }

  /**
   * Adds NGrams up to the specified length to the current instance.
   *
   * @param ngram the tokens to build the uni-grams, bi-grams, tri-grams, ..
   *     from.
   * @param minLength - minimal length
   * @param maxLength - maximal length
   */
  public void add(StringList ngram, int minLength, int maxLength) {

    if (minLength < 1 || maxLength < 1)
      throw new IllegalArgumentException("minLength and maxLength param must be at least 1. " +
          "minLength=" + minLength + ", maxLength= " + maxLength);

    if (minLength > maxLength)
      throw new IllegalArgumentException("minLength param must not be larger than " +
          "maxLength param. minLength=" + minLength + ", maxLength= " + maxLength);

    for (int lengthIndex = minLength; lengthIndex < maxLength + 1; lengthIndex++) {
      for (int textIndex = 0;
          textIndex + lengthIndex - 1 < ngram.size(); textIndex++) {

        String[] grams = new String[lengthIndex];

        for (int i = textIndex; i < textIndex + lengthIndex; i++) {
          grams[i - textIndex] = ngram.getToken(i);
        }

        add(new StringList(grams));
      }
    }
  }

  /**
   * Adds character NGrams to the current instance.
   *
   * @param chars
   * @param minLength
   * @param maxLength
   */
  public void add(CharSequence chars, int minLength, int maxLength) {

    for (int lengthIndex = minLength; lengthIndex < maxLength + 1; lengthIndex++) {
      for (int textIndex = 0;
          textIndex + lengthIndex - 1 < chars.length(); textIndex++) {

        String gram = StringUtil.toLowerCase(
            chars.subSequence(textIndex, textIndex + lengthIndex));

        add(new StringList(new String[]{gram}));
      }
    }
  }

  /**
   * Removes the specified tokens form the NGram model, they are just dropped.
   *
   * @param tokens
   */
  public void remove(StringList tokens) {
    mNGrams.remove(tokens);
  }

  /**
   * Checks fit he given tokens are contained by the current instance.
   *
   * @param tokens
   *
   * @return true if the ngram is contained
   */
  public boolean contains(StringList tokens) {
    return mNGrams.contains(tokens);
  }

  /**
   * Retrieves the number of {@link StringList} entries in the current instance.
   *
   * @return number of different grams
   */
  public int size() {
    return mNGrams.size();
  }

  /**
   * Retrieves an {@link Iterator} over all {@link StringList} entries.
   *
   * @return iterator over all grams
   */
  @Override
  public Iterator<StringList> iterator() {
    return mNGrams.iterator();
  }

  /**
   * Retrieves the total count of all Ngrams.
   *
   * @return total count of all ngrams
   */
  public int numberOfGrams() {
	  return mNGrams.size();
  }

  /**
   * Creates a dictionary which contain all {@link StringList} which
   * are in the current {@link NGramSet}.
   *
   * Entries which are only different in the case are merged into one.
   *
   * Calling this method is the same as calling {@link #toDictionary(boolean)} with true.
   *
   * @return a dictionary of the ngrams
   */
  public Dictionary toDictionary() {
    return toDictionary(false);
  }

  /**
   * Creates a dictionary which contains all {@link StringList}s which
   * are in the current {@link NGramSet}.
   *
   * @param caseSensitive Specifies whether case distinctions should be kept
   *                      in the creation of the dictionary.
   *
   * @return a dictionary of the ngrams
   */
  public Dictionary toDictionary(boolean caseSensitive) {

    Dictionary dict = new Dictionary(caseSensitive);

    for (StringList stringList : this) {
      dict.put(stringList);
    }

    return dict;
  }

  @Override
  public boolean equals(Object obj) {
    boolean result;

    if (obj == this) {
      result = true;
    }
    else if (obj instanceof NGramSet) {
      NGramSet model  = (NGramSet) obj;

      result = mNGrams.equals(model.mNGrams);
    }
    else {
      result = false;
    }

    return result;
  }

  @Override
  public String toString() {
    return "Size: " + size();
  }

  @Override
  public int hashCode() {
    return mNGrams.hashCode();
  }
}
