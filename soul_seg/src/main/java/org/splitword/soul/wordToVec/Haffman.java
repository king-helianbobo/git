package org.splitword.soul.wordToVec;

import java.util.*;

public class Haffman {
	private int layerSize;

	public Haffman(int layerSize) {
		this.layerSize = layerSize;
	}

	private TreeSet<Neuron> set = new TreeSet<Neuron>();

	public void make(Collection<Neuron> neurons) {
		set.addAll(neurons); // 以神经元的词频排序，按词频大小顺序排序
		while (set.size() > 1) {
			merger();
		}
	}

	private void merger() {
		HiddenNeuron hn = new HiddenNeuron(layerSize);
		Neuron min1 = set.pollFirst();
		Neuron min2 = set.pollFirst();
		hn.freq = min1.freq + min2.freq;
		min1.parent = hn;
		min2.parent = hn;
		min1.code = 0;
		min2.code = 1;
		set.add(hn);
	}

}
