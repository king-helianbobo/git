package org.soul.app.crf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.app.crf.*;
import org.soul.treeSplit.SmartForest;

// org.ansj.app.crf.pojo

public abstract class Model {
	private static Log log = LogFactory.getLog(Model.class);

	public static enum MODEL_TYPE {
		CRF, EMM
	};

	protected Template template = null;
	public double[][] status = null;
	protected Map<String, Feature> myGrad;

	protected Map<String, double[][]> tmpFeatures;
	protected SmartForest<double[][]> smartForest = null;

	public int allFeatureCount = 0;

	private List<Element> leftList = null;

	private List<Element> rightList = null;

	public int end1;

	public int end2;

	/**
	 * 根据模板文件解析特征
	 * 
	 * @param template
	 * @throws IOException
	 */
	private void makeSide(int left, int right) throws IOException {
		leftList = new ArrayList<Element>(Math.abs(left));
		for (int i = left; i < 0; i++) {
			leftList.add(new Element((char) ('B' + i)));
		}

		rightList = new ArrayList<Element>(right);
		for (int i = 1; i < right + 1; i++) {
			rightList.add(new Element((char) ('B' + i)));
		}
	}

	public void writeModel(String path) throws FileNotFoundException,
			IOException {
		ObjectOutputStream oos = new ObjectOutputStream(
				new BufferedOutputStream(new GZIPOutputStream(
						new FileOutputStream(path))));
		oos.writeObject(template);
		oos.writeObject(status);
		oos.writeInt(myGrad.size());
		double[] ds = null;
		for (Entry<String, Feature> entry : myGrad.entrySet()) {
			oos.writeUTF(entry.getKey());
			for (int i = 0; i < template.ft.length; i++) {
				ds = entry.getValue().w[i];
				for (int j = 0; j < ds.length; j++) {
					oos.writeByte(j);
					oos.writeFloat((float) ds[j]);
				}
				oos.writeByte(-1);
			}
		}
		oos.flush();
		oos.close();
	}

	public static void writeToFile(Model model, String path)
			throws FileNotFoundException, IOException {
		int i, j;
		FileOutputStream fos = new FileOutputStream(path);
		BufferedOutputStream bos = new BufferedOutputStream(
				new GZIPOutputStream(fos));
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeInt(model.template.left);
		oos.writeInt(model.template.right);
		oos.writeInt(model.template.ft.length);
		for (i = 0; i < model.template.ft.length; i++) {
			oos.writeInt(model.template.ft[i].length);
			for (j = 0; j < model.template.ft[i].length; j++) {
				oos.writeInt(model.template.ft[i][j]);
			}
		}
		oos.writeInt(model.template.tagNum);
		oos.writeInt(model.template.statusMap.size());
		for (Entry<String, Integer> entry : model.template.statusMap.entrySet()) {
			oos.writeUTF(entry.getKey());
			oos.writeInt(entry.getValue());
		}
		oos.writeObject(model.status);
		oos.writeInt(model.tmpFeatures.size());
		for (Entry<String, double[][]> entry : model.tmpFeatures.entrySet()) {
			oos.writeUTF(entry.getKey());
			double[][] value = entry.getValue();
			for (i = 0; i < value.length; i++) {
				for (j = 0; j < value[i].length; j++) {
					oos.writeByte(j);
					oos.writeFloat((float) value[i][j]);
				}
				oos.writeByte(-1);
			}
		}
		oos.flush();
		oos.close();
	}

	private static void fillTemplate(ObjectInputStream ois, Template template)
			throws IOException {
		int left = ois.readInt();
		int right = ois.readInt();
		template.left = left;
		template.right = right;
		// model.template.ft.length = ois.readInt();
		int length = ois.readInt();
		log.info(" length = " + length);
		template.ft = new int[length][0];
		for (int i = 0; i < length; i++) {
			int lenII = ois.readInt();
			log.info(" lengthII = " + lenII);
			if (template.ft[i].length == 0)
				template.ft[i] = new int[lenII];
			for (int j = 0; j < lenII; j++) {
				template.ft[i][j] = ois.readInt();
			}
		}
		template.tagNum = ois.readInt();
		int size = ois.readInt();
		template.statusMap = new HashMap<String, Integer>(4);
		// model.template.ft.length = length;
		for (int i = 0; i < size; i++) {
			String key = ois.readUTF();
			int value = ois.readInt();
			template.statusMap.put(key, value);
		}

	}

	public static Model readModel(InputStream modelStream) throws Exception {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new BufferedInputStream(
					new GZIPInputStream(modelStream)));
			Model model = new Model() {
				// @Override
				// public void writeModel(String path)
				// throws FileNotFoundException, IOException {
				// throw new RuntimeException(
				// "you can not to calculate ,this model only use by cut ");
				// }
			};
			log.info("read object started!");
			model.template = new Template();
			fillTemplate(ois, model.template);
			// model.template = (Template) ois.readObject();
			model.makeSide(model.template.left, model.template.right);
			int tagNum = model.template.tagNum;
			int featureNum = model.template.ft.length;

			model.status = (double[][]) ois.readObject();
			model.smartForest = new SmartForest<double[][]>();
			double[][] weight = null;
			String key = null;
			int b = 0;
			int featureCount = ois.readInt();
			for (int i = 0; i < featureCount; i++) {
				key = ois.readUTF();
				// log.info("key = " + key);
				weight = new double[featureNum][0];
				for (int j = 0; j < featureNum; j++) {
					while ((b = ois.readByte()) != -1) {
						if (weight[j].length == 0) {
							weight[j] = new double[tagNum];
						}
						weight[j][b] = ois.readFloat();
					}
				}
				if (model.tmpFeatures == null)
					model.tmpFeatures = new HashMap<String, double[][]>();
				model.tmpFeatures.put(key, weight);
				model.smartForest.add(key, weight);
			}
			logModelInfo(model);

			return model;
		} finally {
			if (ois != null) {
				ois.close();
			}
		}
	}

	private static void logModelInfo(Model model) {
		int tagNum = model.template.tagNum;
		int featureNum = model.template.ft.length;
		log.info("featureNum = " + featureNum + ", tagNum = " + tagNum);
		for (int i = 0; i < featureNum; i++) {
			StringBuilder builder = new StringBuilder();
			for (int j = 0; j < model.template.ft[i].length; j++) {
				builder.append(model.template.ft[i][j] + " ");
			}
			log.info("feature: " + builder.toString());
		}
		Set<Entry<String, Integer>> entrySet = model.template.statusMap
				.entrySet();
		for (Entry<String, Integer> entry : entrySet) {
			log.info("statusMap: " + entry.getKey() + ", " + entry.getValue());
		}
		for (int i = 0; i < model.status.length; i++) {
			StringBuilder builder = new StringBuilder();
			for (int j = 0; j < model.status[i].length; j++) {
				builder.append(model.status[i][j] + " ");
			}
			log.info("transW: " + builder.toString());
		}
		log.info("featureNum = " + model.template.ft.length + ", tagNum = "
				+ model.template.tagNum);
	}

	public static Model loadModel(String modelPath) throws Exception {
		return loadModel(new FileInputStream(modelPath));
	}

	public static Model loadModel(InputStream modelStream) throws Exception {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new BufferedInputStream(
					new GZIPInputStream(modelStream)));
			Model model = new Model() {
			};
			log.info("read object started!");
			model.template = (Template) ois.readObject();
			model.makeSide(model.template.left, model.template.right);
			model.smartForest = new SmartForest<double[][]>();
			model.status = (double[][]) ois.readObject();
			int tagNum = model.template.tagNum;
			int featureNum = model.template.ft.length;

			int featureCount = ois.readInt();
			log.info("featureCount = " + featureCount);
			for (int i = 0; i < featureCount; i++) {
				int b = 0;
				String key = ois.readUTF();
				log.info("key = " + key);
				double[][] weight = new double[featureNum][0];
				for (int j = 0; j < featureNum; j++) {
					while ((b = ois.readByte()) != -1) {
						if (weight[j].length == 0)
							weight[j] = new double[tagNum];
						weight[j][b] = ois.readFloat();
					}
				}
				model.smartForest.add(key, weight);
				if (model.tmpFeatures == null)
					model.tmpFeatures = new HashMap<String, double[][]>();
				model.tmpFeatures.put(key, weight);
			}
			logModelInfo(model);
			return model;
		} finally {
			if (ois != null) {
				ois.close();
			}
		}
	}

	public double[] getFeature(int featureIndex, char... chars) {
		SmartForest<double[][]> sf = smartForest;
		sf = sf.getBranch(chars);
		if (sf == null || sf.getParam() == null) {
			return null;
		}
		return sf.getParam()[featureIndex];
	}

	/**
	 * tag转移率
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public double tagRate(int s1, int s2) {
		return status[s1][s2];
	}

}