package org.splitword.soul.wordToVec;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.splitword.soul.treeSplit.*;

public class VectorLearn {

	private Map<String, Neuron> wordMap = new HashMap<String, Neuron>();
	private Log log = LogFactory.getLog(VectorLearn.class);

	private int layerSize = 200;// 每个词特征的维度

	private int window = 5; // 上下文窗口大小

	private double sample = 1e-3;
	private double alpha = 0.025;
	private double startingAlpha = alpha;

	public int EXP_TABLE_SIZE = 1000;

	private Boolean isCbow = false;

	private double[] expTable = new double[EXP_TABLE_SIZE];

	private int trainWordsCount = 0;

	private int MAX_EXP = 6;

	public VectorLearn(Boolean isCbow, Integer layerSize, Integer window,
			Double alpha, Double sample) {
		createExpTable();
		if (isCbow != null) {
			this.isCbow = isCbow;
		}
		if (layerSize != null)
			this.layerSize = layerSize;
		if (window != null)
			this.window = window;
		if (alpha != null)
			this.alpha = alpha;
		if (sample != null)
			this.sample = sample;
	}

	public VectorLearn() {
		createExpTable();
	}

	/**
	 * trainModel
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	private void trainModel(File file) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)));
		String temp = null;
		long nextRandom = 5;
		int wordCount = 0;
		int lastWordCount = 0;
		int wordCountActual = 0;
		while ((temp = br.readLine()) != null) {
			if (wordCount - lastWordCount > 10000) {
				log.info("alpha:"
						+ alpha
						+ "\tProgress: "
						+ (int) (wordCountActual
								/ (double) (trainWordsCount + 1) * 100) + "%");
				wordCountActual += wordCount - lastWordCount;
				lastWordCount = wordCount;
				alpha = startingAlpha
						* (1 - wordCountActual / (double) (trainWordsCount + 1));
				if (alpha < startingAlpha * 0.0001) {
					alpha = startingAlpha * 0.0001;
				}
			}
			String[] strs = temp.split(" ");
			wordCount += strs.length;
			List<WordNeuron> sentence = new ArrayList<WordNeuron>();
			for (int i = 0; i < strs.length; i++) {
				Neuron entry = wordMap.get(strs[i]);
				if (entry == null) {
					continue;
				}
				// randomly discards frequent words while
				// keeping the ranking same
				if (sample > 0) {
					double ran = (Math.sqrt(entry.freq
							/ (sample * trainWordsCount)) + 1)
							* (sample * trainWordsCount) / entry.freq;
					nextRandom = nextRandom * 25214903917L + 11;
					if (ran < (nextRandom & 0xFFFF) / (double) 65536) {
						continue;
					}
					sentence.add((WordNeuron) entry);
				}
			}

			for (int index = 0; index < sentence.size(); index++) {
				nextRandom = nextRandom * 25214903917L + 11;
				if (isCbow) {
					cbowGram(index, sentence, (int) nextRandom % window);
				} else {
					skipGram(index, sentence, (int) nextRandom % window);
				}
			}
		}
	}

	/**
	 * skip gram 模型训练
	 * 
	 * @param sentence
	 * @param neu1
	 */
	private void skipGram(int index, List<WordNeuron> sentence, int b) {
		// TODO Auto-generated method stub
		// double[] neu1 = new double[layerSize];//神经元
		WordNeuron word = sentence.get(index);
		int a, c = 0;
		for (a = b; a < window * 2 + 1 - b; a++) {
			if (a == window) {
				continue;
			}
			c = index - window + a;
			if (c < 0 || c >= sentence.size()) {
				continue;
			}

			double[] neu1e = new double[layerSize];// 误差项
			// HIERARCHICAL SOFTMAX
			List<Neuron> neurons = word.getNeurons();
			WordNeuron we = sentence.get(c);
			for (int i = 0; i < neurons.size(); i++) {
				HiddenNeuron out = (HiddenNeuron) neurons.get(i);
				double f = 0;
				// Propagate hidden -> output
				for (int j = 0; j < layerSize; j++) {
					f += we.syn0[j] * out.syn1[j];
				}
				if (f <= -MAX_EXP || f >= MAX_EXP) {
					continue;
				} else {
					f = (f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2);
					f = expTable[(int) f];
				}
				// 'g' is the gradient multiplied by the learning rate
				double g = (1 - word.codeArr[i] - f) * alpha;
				for (c = 0; c < layerSize; c++) {
					neu1e[c] += g * out.syn1[c];
				}
				// Learn weights hidden -> output
				for (c = 0; c < layerSize; c++) {
					out.syn1[c] += g * we.syn0[c];
				}
			}

			// Learn weights input -> hidden
			for (int j = 0; j < layerSize; j++) {
				we.syn0[j] += neu1e[j];
			}
		}

	}

	/**
	 * 词袋模型
	 * 
	 * @param index
	 *            ，第几个词
	 * @param sentence
	 *            ，词列表
	 * @param smallWin
	 *            ,窗口大小
	 */
	private void cbowGram(int index, List<WordNeuron> sentence, int smallWin) {
		WordNeuron word = sentence.get(index);
		int a, c = 0;
		List<Neuron> neurons = word.getNeurons();
		double[] neu_a = new double[layerSize];// 误差项
		double[] neu_b = new double[layerSize];// 误差项
		WordNeuron last_word;

		for (a = smallWin; a < window * 2 + 1 - smallWin; a++) {
			if (a != window) {
				c = index - window + a;
				if (c < 0)
					continue;
				if (c >= sentence.size())
					continue;
				last_word = sentence.get(c);
				if (last_word == null)
					continue;
				for (c = 0; c < layerSize; c++)
					neu_b[c] += last_word.syn0[c];
			}
		}
		// //HIERARCHICAL SOFTMAX
		// WordNeuron we = sentence.get(c);

		for (int d = 0; d < neurons.size(); d++) {
			HiddenNeuron out = (HiddenNeuron) neurons.get(d);
			double f = 0; // Propagate hidden -> output
			for (c = 0; c < layerSize; c++)
				f += neu_b[c] * out.syn1[c];
			if (f <= -MAX_EXP)
				continue;
			else if (f >= MAX_EXP)
				continue;
			else
				f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];
			// 'g' is the gradient multiplied by the learning rate
			// double g = (1 - word.codeArr[d] - f) * alpha;
			// double g = f*(1-f)*( word.codeArr[i] - f) * alpha;
			double g = f * (1 - f) * (word.codeArr[d] - f) * alpha;
			//
			for (c = 0; c < layerSize; c++) {
				neu_a[c] += g * out.syn1[c];
			}
			// Learn weights hidden -> output
			for (c = 0; c < layerSize; c++) {
				out.syn1[c] += g * neu_b[c];
			}
		}

		for (a = smallWin; a < window * 2 + 1 - smallWin; a++) {
			if (a != window) {
				c = index - window + a;
				if (c < 0)
					continue;
				if (c >= sentence.size())
					continue;
				last_word = sentence.get(c);
				if (last_word == null)
					continue;
				for (c = 0; c < layerSize; c++)
					last_word.syn0[c] += neu_a[c];
			}

		}
	}

	/**
	 * 统计词频
	 * 
	 * @param file
	 * @throws IOException
	 */
	private void readVocab(File file) throws IOException {
		MapCount<String> mc = new MapCount<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)));
		String temp = null;
		while ((temp = reader.readLine()) != null) {
			String[] split = temp.split(" ");
			trainWordsCount += split.length; // 总共的词个数
			for (String string : split) {
				mc.add(string); // 统计每个词的出现次数
			}
		}

		for (Entry<String, Integer> element : mc.get().entrySet()) {
			wordMap.put(element.getKey(), new WordNeuron(element.getKey(),
					element.getValue(), layerSize));
		}
	}

	/**
	 * f(x) = x / (x + 1)
	 */
	private void createExpTable() {
		for (int i = 0; i < EXP_TABLE_SIZE; i++) {
			expTable[i] = Math
					.exp(((i / (double) EXP_TABLE_SIZE * 2 - 1) * MAX_EXP));
			expTable[i] = expTable[i] / (expTable[i] + 1);
		}
	}

	/**
	 * 根据文件学习
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void learnFile(File file) throws IOException {
		readVocab(file);
		// log.info(file.toString());
		new Haffman(layerSize).make(wordMap.values()); // 构建哈夫曼树
		trainModel(file);
	}

	/**
	 * save trained model to local file system
	 */
	@SuppressWarnings("resource")
	public void saveModel(File file) {
		try {
			DataOutputStream dataOutputStream = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(file)));
			dataOutputStream.writeInt(wordMap.size());
			dataOutputStream.writeInt(layerSize);
			double[] syn0 = null;
			for (Entry<String, Neuron> element : wordMap.entrySet()) {
				dataOutputStream.writeUTF(element.getKey());
				log.info(element.getKey());
				syn0 = ((WordNeuron) element.getValue()).syn0;
				for (double d : syn0) {
					dataOutputStream.writeFloat(((Double) d).floatValue());
				}
			}
			dataOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getLayerSize() {
		return layerSize;
	}

	public void setLayerSize(int layerSize) {
		this.layerSize = layerSize;
	}

	public int getWindow() {
		return window;
	}

	public void setWindow(int window) {
		this.window = window;
	}

	public double getSample() {
		return sample;
	}

	public void setSample(double sample) {
		this.sample = sample;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
		this.startingAlpha = alpha;
	}

	public Boolean getIsCbow() {
		return isCbow;
	}

	public void setIsCbow(Boolean isCbow) {
		this.isCbow = isCbow;
	}

}
