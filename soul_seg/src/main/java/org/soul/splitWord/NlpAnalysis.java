package org.soul.splitWord;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.soul.domain.Graph;
import org.soul.domain.Term;
import org.soul.recognition.NatureRecognition;
import org.soul.recognition.NewWordRecognition;
import org.soul.recognition.NumberRecognition;
import org.soul.recognition.UserDefineRecognition;

/**
 * Native language processing
 */
public class NlpAnalysis extends Analysis {
	private static Log log = LogFactory.getLog(NlpAnalysis.class);
	private LearnTool learn = null;

	public NlpAnalysis(Reader reader, LearnTool learn) {
		super(reader);
		this.learn = learn;
	}

	private NlpAnalysis(LearnTool learn) {
		this.learn = learn;
	}

	@Override
	protected List<Term> getResult(final Graph graph) {
		Merger merger = new Merger() {
			@Override
			public List<Term> merge() {
				graph.walkPath(); // get optimal path
				log.info(getResult());
				if (graph.hasNum)
					NumberRecognition.recognition(graph.terms);
				log.info(getResult());

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

	public static List<Term> parse(String str, LearnTool learn) {
		return new NlpAnalysis(learn).parseStr(str);
	}

	public static List<Term> parse(String str) {
		return new NlpAnalysis(new LearnTool()).parseStr(str);
	}
}
