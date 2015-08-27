package aaa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.search.spell.NGramDistance;

public class Main {

	public static void main(String[] args) {
		new Main();

	}

	public Scanner initializeScanner(File file) {
		try {
			return new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Main() {
		String queries = "C:/Users/User/Desktop/New folder (3)/project1/query/queries.10K.txt"; //Path of the Query File
		String tweets = "C:/Users/User/Desktop/New folder (3)/project1/tweets/tweets.3K.txt"; //Path of the Tweet File
		//String queries = "queries.txt";
		//String tweets = "tweets.txt";
		File queriesFile = new File(queries); //
		File tweetsFile = new File(tweets);
		if (!queriesFile.exists()) {
			System.out.println("Invalid Queries File");
			return;
		}
		if (!tweetsFile.exists()) {
			System.out.println("Invalid Tweets File");
			return;
		}
		Scanner queriesLineReader = initializeScanner(queriesFile);
		Scanner tweetsLineReader = initializeScanner(tweetsFile);
		Matcher matcher = null;
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("output-q10k-t3k.csv");
			writer.println("Query,Local,NGram,Tweet ID,Tweet Content");
			writer.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double MIN_VALUE = -999999999.0;

		while (queriesLineReader.hasNextLine()) {
			ArrayList<String> queryWords = new ArrayList<String>();
			ArrayList<Double> nGramDistMAX = new ArrayList<Double>();
			String queryLine = queriesLineReader.nextLine();
			queryLine = queryLine.trim();
			if (!queryLine.equals("")) {
				Scanner queryWordReader = new Scanner(queryLine);
				// for ngram : get every query word
				while (queryWordReader.hasNext()) {
					String queryWord = queryWordReader.next();
					// System.out.print(queryWord + " ");
					queryWords.add(queryWord);
					nGramDistMAX.add(MIN_VALUE);
				}
				queryWordReader.close();
				String output_queryLine = "" + queryLine;
				while (tweetsLineReader.hasNextLine()) {
					String tweetLine = tweetsLineReader.nextLine();
					String output_tweetLine = "Tweet Line : " + tweetLine + " | ";
					tweetLine = tweetLine.trim();
					matcher = Pattern.compile("^(\\d+)(\\s*)(.+)$").matcher(tweetLine);
					matcher.find();
					String tweetID = null;
					String tweetContent = null;
					try {
						tweetID = matcher.group(1);
						tweetContent = matcher.group(3);
					} catch (IllegalStateException e) {
						// continue;
						System.out.println("PATERN MISMATCH for Tweet Line :  " + tweetLine);
					}

					// for ngram : get MAX values 
					Scanner tweetWordReader = new Scanner(tweetContent);
					while (tweetWordReader.hasNext()) {
						String tweetWord = tweetWordReader.next();
						// System.out.print(tweetWord + " ");
						for (int i = 0; i < queryWords.size(); i++) {
							double currentNGramDist = nGramDistMAX.get(i);
							double newNGramDist = new NGramDistance(2).getDistance(queryWords.get(i), tweetWord);
							if (newNGramDist > currentNGramDist) {
								nGramDistMAX.set(i, newNGramDist);
							}
						}
					}
					tweetWordReader.close();

					// for ngram : get average
					double sumsNGramDist = 0.0;
					double queryWordsCount = queryWords.size();
					for (int i = 0; i < queryWords.size(); i++) {
						sumsNGramDist += nGramDistMAX.get(i);
						nGramDistMAX.set(i, MIN_VALUE);
					}

					double averageNGramDist = sumsNGramDist / queryWordsCount;

					// for local distance : percentage
					//String queryLineWithoutSpaces = queryLine.replaceAll("\\s", "");
					//String tweetContentWithoutSpaces = tweetContent.replaceAll("\\s", "");
					SmithWaterman sw = new SmithWaterman(queryLine, tweetContent);
					double percentage = sw.getPercentage();
					String output_localPercentage = (new DecimalFormat("000.00")).format(percentage) + " %";
					String output_ngrampercentage = (new DecimalFormat("000.00")).format(averageNGramDist*100) + " %";

				}
				tweetsLineReader = initializeScanner(tweetsFile);
			}
			System.out.println("Query Line (" + queryLine + ") Done");
		}
		System.out.println("All Done");
	}
}
