package racingcar.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CarsTest {

    @Test
    void 참가자_등록_테스트() {
        //given
        final Car car1 = new Car("test1");
        final Car car2 = new Car("test2");
        //when
        //then
        List<Car> cars = new Cars(List.of(car1, car2)).getCars();

        Assertions.assertThat(cars.size()).isEqualTo(2);
        Assertions.assertThat(cars.get(0).getName()).isEqualTo("test1");
        Assertions.assertThat(cars.get(1).getName()).isEqualTo("test2");
    }

    @Test
    void 참가자_이름은_중복될_수_없다() {
        //given
        final Car car1 = new Car("test1");
        final Car car2 = new Car("test1");
        //when then
        assertThatThrownBy(() -> {
            new Cars(List.of(car1, car2));
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("자동차 이름은 중복될 수 없습니다.");
    }

    @Test
    void 가장_멀리간_차량이_승자이다() {
        //given
        final Car car1 = new Car("test1");
        final Car car2 = new Car("test2");
        final Car car3 = new Car("test3");
        Cars cars = new Cars(List.of(car1, car2, car3));

        // when
        car1.drive(4);
        car1.drive(4);
        car2.drive(4);
        List<Car> winners = cars.findWinners();

        // then
        assertThat(winners.size()).isEqualTo(1);
        assertThat(winners.get(0).getName()).isEqualTo("test1");
    }

    @Test
    void 가장_멀리간_차량이_여러대이면_승자는_여러명이다() {
        //given
        final Car car1 = new Car("test1");
        final Car car2 = new Car("test2");
        final Car car3 = new Car("test3");
        Cars cars = new Cars(List.of(car1, car2, car3));

        // when
        car1.drive(4);
        car1.drive(4);
        car2.drive(4);
        car2.drive(4);
        car3.drive(4);
        List<Car> winners = cars.findWinners();

        // then
        assertThat(winners.size()).isEqualTo(2);
        assertThat(winners.stream()
                .map(Car::getName)
                .collect(Collectors.toList())).containsExactly("test1", "test2");
    }

}
