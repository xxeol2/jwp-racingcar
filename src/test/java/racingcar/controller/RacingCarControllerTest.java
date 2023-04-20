package racingcar.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import racingcar.dto.PlayRequest;
import racingcar.dto.PlayResponse;
import racingcar.entity.Player;
import racingcar.service.RacingCarService;

@WebMvcTest
class RacingCarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RacingCarService racingCarService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 게임을_진행한다() throws Exception {
        PlayResponse response = PlayResponse.of(
                "car2",
                List.of(new Player(1, "car1", 10, 1), new Player(2, "car2", 10, 1)));

        given(racingCarService.play(any(PlayRequest.class)))
                .willReturn(response);

        PlayRequest playRequest = new PlayRequest(List.of("car1", "car2"), 4);
        String request = objectMapper.writeValueAsString(playRequest);

        mockMvc.perform(post("/plays")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("winners").value("car2"))
                .andExpect(jsonPath("racingCars", hasSize(2)))
                .andDo(print());
    }

    @Test
    void 이력을_조회한다() throws Exception {
        List<PlayResponse> response = List.of(
                PlayResponse.of(
                        "car2",
                        List.of(new Player(1, "car1", 8, 1), new Player(2, "car2", 10, 1))),
                PlayResponse.of(
                        "car3",
                        List.of(new Player(1, "car3", 10, 2)))
        );

        given(racingCarService.findHistory()).willReturn(response);

        mockMvc.perform(get("/plays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].winners").value("car2"))
                .andExpect(jsonPath("$[0].racingCars", hasSize(2)))
                .andExpect(jsonPath("$[0].racingCars[0].name").value("car1"))
                .andExpect(jsonPath("$[0].racingCars[0].position").value(8))
                .andExpect(jsonPath("$[0].racingCars[1].name").value("car2"))
                .andExpect(jsonPath("$[0].racingCars[1].position").value(10))
                .andExpect(jsonPath("$[1].winners").value("car3"))
                .andExpect(jsonPath("$[1].racingCars", hasSize(1)))
                .andExpect(jsonPath("$[1].racingCars[0].name").value("car3"));
    }

    @ValueSource(strings = {"", "123456"})
    @ParameterizedTest
    void 잘못된_자동차_이름길이(String name) throws Exception {
        PlayRequest playRequest = new PlayRequest(List.of(name), 4);

        performBadRequest(playRequest);
    }

    @ValueSource(ints = {0, 11})
    @ParameterizedTest
    void 잘못된_시도횟수(int count) throws Exception {
        PlayRequest playRequest = new PlayRequest(List.of("애쉬"), count);

        performBadRequest(playRequest);
    }

    @ValueSource(ints = {0, 6})
    @ParameterizedTest
    void 잘못된_참여자수(int playerSize) throws Exception {
        List<String> playerNames = new ArrayList<>();
        for (int i = 0; i < playerSize; i++) {
            playerNames.add(Integer.toString(i));
        }
        PlayRequest playRequest = new PlayRequest(List.of("애쉬"), 5);

        performBadRequest(playRequest);
    }

    private void performBadRequest(PlayRequest playRequest) throws Exception {
        String request = objectMapper.writeValueAsString(playRequest);
        given(racingCarService.play(any(PlayRequest.class))).willReturn(PlayResponse.of("애쉬", List.of()));
        mockMvc.perform(post("/plays")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
