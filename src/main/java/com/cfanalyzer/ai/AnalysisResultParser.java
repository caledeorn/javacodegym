package com.cfanalyzer.ai;

import com.cfanalyzer.model.AnalysisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

public class AnalysisResultParser {

    private static final ObjectMapper objectMapper =
            new ObjectMapper();

    public AnalysisResult parse(String apiResponse)
            throws Exception {

        // parse trực tiếp JSON AI trả về
        JsonNode jsonNode =
                objectMapper.readTree(apiResponse);

        return AnalysisResult.builder()

                .dataStructuresUsed(
                        jsonNode.path("data_structures").toString()
                )

                .dsPrimary(
                        jsonNode.path("ds_primary").asText()
                )

                .dsComplexityScore(
                        jsonNode.path("ds_complexity_score").asInt()
                )

                .algorithmsUsed(
                        jsonNode.path("algorithms").toString()
                )

                .algoPrimary(
                        jsonNode.path("algo_primary").asText()
                )

                .algoCategory(
                        jsonNode.path("algo_category").asText()
                )

                .algoComplexityScore(
                        jsonNode.path("algo_complexity_score").asInt()
                )

                .timeComplexity(
                        jsonNode.path("time_complexity").asText()
                )

                .spaceComplexity(
                        jsonNode.path("space_complexity").asText()
                )

                .aiProbability(
                        jsonNode.path("ai_probability").asDouble()
                )

                .aiIndicators(
                        jsonNode.path("ai_indicators").toString()
                )

                .aiExplanation(
                        jsonNode.path("ai_explanation").asText()
                )

                .codeQualityScore(
                        jsonNode.path("code_quality_score").asInt()
                )

                .readabilityScore(
                        jsonNode.path("readability_score").asInt()
                )

                .originalityScore(
                        jsonNode.path("originality_score").asInt()
                )

                .difficultyEstimate(
                        jsonNode.path("difficulty_estimate").asText()
                )

                .overallSummary(
                        jsonNode.path("overall_summary").asText()
                )

                .rawAiResponse(apiResponse)

                .analyzedAt(LocalDateTime.now())

                .build();
    }
}