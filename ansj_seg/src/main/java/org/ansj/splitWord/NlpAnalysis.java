package org.ansj.splitWord;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.ansj.domain.Graph;
import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.recognition.NewWordRecognition;
import org.ansj.recognition.NumberRecognition;
import org.ansj.recognition.UserDefineRecognition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 自然语言分词,结果不稳定但是很全面
 * 
 * Native language processing
 * 
 */
public class NlpAnalysis extends Analysis {
	private static Log log = LogFactory.getLog(NlpAnalysis.class);
	private LearnTool learn = null;

	public NlpAnalysis(Reader reader, LearnTool learn) {
		super(reader);
		this.learn = learn;
	}

	@Override
	protected List<Term> getResult(final Graph graph) {
		Merger merger = new Merger() {
			@Override
			public List<Term> merge() {
				graph.walkPath(); // 获得最优路径
				log.info(getResult());
				// if (graph.hasNum)
				// NumberRecognition.recognition(graph.terms);
				// log.info(getResult());

				List<Term> result = getResult();
				new NatureRecognition(result).recognition(); // 词性标注
				log.info(getResult());

				// 新词发现训练
				learn.learn(graph);
				log.info(getResult());

				// 用户自定义词典
				new UserDefineRecognition(graph.terms).recognition();
				// log.info(getResult());
				// graph.rmLittlePath();
				graph.walkPathByScore();
				log.info(getResult());

				new NewWordRecognition(graph.terms, learn).recognition();
				graph.walkPathByScore();
				log.info(getResult());
				// 优化后重新获得最优路径
				return getResult();
			}

			private List<Term> getResult() {
				List<Term> result = new ArrayList<Term>();
				int length = graph.terms.length - 1;
				for (int i = 0; i < length; i++) {
					if (graph.terms[i] != null) {
						result.add(graph.terms[i]);
					}
				}
				return result;
			}
		};
		return merger.merge();
	}

	private NlpAnalysis(LearnTool learn) {
		this.learn = learn;
	};

	public static List<Term> parse(String str, LearnTool learn) {
		return new NlpAnalysis(learn).parseStr(str);
	}

	public static List<Term> parse(String str) {
		return new NlpAnalysis(new LearnTool()).parseStr(str);
	}
}
