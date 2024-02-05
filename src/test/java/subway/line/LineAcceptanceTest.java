package subway.line;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import subway.domain.line.dto.response.LineResponse;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@Sql(scripts = "/truncate.sql", executionPhase = BEFORE_TEST_METHOD)
@DisplayName("지하철 노선 관리에 대한 인수테스트입니다.")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class LineAcceptanceTest implements LineFixture {

    /**
     * When 지하철 노선을 생성하면
     * Then 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다.
     */
    @DisplayName("지하철 노선을 생성하고 노선 목록 조회 시 생성한 노선을 찾을 수 있다.")
    @Test
    void createStationLine() {
        // when
        ExtractableResponse<Response> response = createLineByNameAndStation("신분당선");

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        ExtractableResponse<Response> lineResponse = getLinesExtractableResponse();

        assertThat(lineResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(lineResponse.body().jsonPath().getList("name", String.class))
                .containsExactly("신분당선");
    }

    /**
     * Given 2개의 지하철 노선을 생성하고
     * When 지하철 노선 목록을 조회하면
     * Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
     */
    @DisplayName("2개의 지하철 노선을 생성한 후 지하철 목록 조회가 가능한지 확인한다.")
    @Test
    void getLines() {
        // given
        createLineByNameAndStation("신분당선");
        createLineByNameAndStation("수인분당선");

        // when
        ExtractableResponse<Response> lineResponse = getLinesExtractableResponse();

        // then
        assertThat(lineResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(lineResponse.jsonPath().getList("name", String.class)).hasSize(2)
                .containsExactlyInAnyOrder("수인분당선", "신분당선");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 조회하면
     * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
     */
    @DisplayName("생성한 지하철 노선에 대한 정보를 조회할 수 있다.")
    @Test
    void getLineById() {
        // given
        long lineId = getLineId();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/lines/" + lineId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.as(LineResponse.class).getId()).isEqualTo(lineId);
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 수정하면
     * Then 해당 지하철 노선 정보는 수정된다.
     */
    @DisplayName("생성된 지하철 노선의 정보를 수정할 수 있다.")
    @Test
    void updateLine() {
        // given
        long lineId = getLineId();

        Map<String, Object> params = new HashMap<>();
        params.put("name", "변경된 지하철");
        params.put("color", "bg-red-600");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().put("/lines/" + lineId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getString("name")).isEqualTo("변경된 지하철");
        assertThat(response.jsonPath().getString("color")).isEqualTo("bg-red-600");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 삭제하면
     * Then 해당 지하철 노선 정보는 삭제된다.
     */
    @DisplayName("지하철 노선을 삭제한다.")
    @Test
    void deleteLineById() {
        // given
        long lineId = getLineId();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().delete("/lines/" + lineId)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    private static ExtractableResponse<Response> getLinesExtractableResponse() {
        ExtractableResponse<Response> lineResponse = RestAssured.given().log().all()
                .when().get("/lines")
                .then().log().all()
                .extract();
        return lineResponse;
    }

    private long getLineId() {
        long lindId = createLineByNameAndStation("신분당선").jsonPath().getLong("id");
        return lindId;
    }

}
