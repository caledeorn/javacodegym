package com.cfanalyzer.ai;

public class CodeAnalysisPromptBuilder {

    public static String buildPrompt(
            String language,
            String problemName,
            String problemId,
            String codeContent
    ) {

        return """
        Bạn là AI phân tích code Codeforces.

        Chỉ trả JSON hợp lệ.
        Không markdown.
        Không giải thích.
        Không ```json.

        JSON format:

        {
          "data_structures": [],
          "ds_primary": "",
          "ds_complexity_score": 0,

          "algorithms": [],
          "algo_primary": "",
          "algo_category": "",
          "algo_complexity_score": 0,

          "time_complexity": "",
          "space_complexity": "",

          "ai_probability": 0.0,
          "ai_indicators": [],
          "ai_explanation": "",

          "code_quality_score": 0,
          "readability_score": 0,
          "originality_score": 0,

          "difficulty_estimate": "",
          "overall_summary": ""
        }

        Rules:
        - ds_complexity_score: 1-10
        - algo_complexity_score: 1-10
        - code_quality_score: 1-100
        - readability_score: 1-100
        - originality_score: 1-100
        - ai_probability: 0.0-1.0

        Problem:
        %s (%s)

        Language:
        %s

        Code:
        %s
        """.formatted(
                problemName,
                problemId,
                language,
                codeContent
        );
    }
}