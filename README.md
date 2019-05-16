Jane Liu
Big Data Science Homework 3 - Twitter Keyword Extraction

* Running this program as a batch process takes a long time (over 12 hours). I created a smaller dataset of the originals in sentiment140_part.csv located in the /data/ folder; the program can process this file in about 5-10 minutes. A variable called String filename located at the top of TwitterExtract.java and BestExtractor.java can be changed to use this smaller file.


Files and folder in this project:
hw3.jar
hw3.s
README.txt
JaneLiu_HW3_TwitterKeywordExtraction.pdf
/src/
/data/
/lib/
/textrank/		// TextRank library


Requirements and Info:

All included folders should be in the same directory as hw3.jar.

The /src/ folder contains all the source files: 
TwitterExtract.java (main)
BestExtractor.java
StanfordNLP.java

The /data/ folder contains the file twitter_stopwords.txt. The sentiment140.csv dataset should be copied into this folder too.

The /lib/ folder contains the Stanford Core NLP jar files, which are not included. It is assumed a copy of Stanford Core NLP jar files can be copied locally to a /lib/ folder.

This program was written in Java 8. It is unknown if the program will run correctly for other versions of Java.


Instructions:

A batch processing file hw3.s is used to run this program. The /scratch/user/ directory in hw3.s should point to the current working directory. The email address can be updated to get alerts.

From the Prince command line please type the following:
$ sbatch hw3.s

If quicker results are needed, the filename variable in TwitterExtract.java and BestExtractor.java can be changed to "data/sentiment140_part.csv" and the program can be run at the terminal using:
$ module purge
$ module load jdk/1.8.0_111
$ srun --mem=100GB --time=00:20:00 --cpus-per-task 1 --pty $SHELL
$ java -cp hw3.jar:./lib/* hw3.TwitterExtract


Expected Output:

Most results are output to the results.txt file located in the working directory. The results for my best keyword extraction method is output to bestextraction.txt in the working directory.