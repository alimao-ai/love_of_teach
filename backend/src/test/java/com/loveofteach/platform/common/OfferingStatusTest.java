package com.loveofteach.platform.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OfferingStatusTest {

    @Test
    void parsesEnglishAndChineseStatuses() {
        assertThat(OfferingStatus.from("open")).isEqualTo(OfferingStatus.OPEN);
        assertThat(OfferingStatus.from("待开课")).isEqualTo(OfferingStatus.PLANNED);
        assertThat(OfferingStatus.from("已停课")).isEqualTo(OfferingStatus.CLOSED);
    }

    @Test
    void rejectsUnknownStatus() {
        assertThatThrownBy(() -> OfferingStatus.from("ARCHIVED"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不支持的开课状态");
    }
}
