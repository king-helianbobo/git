package org.ansj.demo;

import java.util.List;

import org.soul.domain.Term;
import org.soul.splitWord.IndexAnalysis;

public class IndexPaserDemo {
	public static void main(String[] args) {
		System.out.println(IndexAnalysis.parse("上海虹桥机场南路"));
	}
}
