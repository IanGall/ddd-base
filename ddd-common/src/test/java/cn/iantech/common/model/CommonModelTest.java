package cn.iantech.common.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommonModelTest {

    @Test
    void shouldExposeResponseContractThroughConstructorsAndBuilder() {
        Response<String> response = new Response<>("SUCCESS", "成功", "data");
        Response<String> same = Response.<String>builder()
                .code("SUCCESS")
                .info("成功")
                .data("data")
                .build();

        assertEquals(same, response);
        assertEquals(same.hashCode(), response.hashCode());
        assertTrue(response.toString().contains("SUCCESS"));

        response.setData("updated");
        assertEquals("updated", response.getData());
        assertNotEquals(same, response);
    }

    @Test
    void shouldExposePageRequestAndResponseContracts() {
        PageRequest request = new PageRequest(2, 20);
        PageRequest sameRequest = PageRequest.builder().pageNum(2).pageSize(20).build();
        PageResponse<String> response = new PageResponse<>(21L, 2, 20, List.of("item"));
        PageResponse<String> sameResponse = PageResponse.<String>builder()
                .total(21L)
                .pageNum(2)
                .pageSize(20)
                .list(List.of("item"))
                .build();

        assertEquals(sameRequest, request);
        assertEquals(sameResponse, response);
        assertTrue(request.toString().contains("pageNum=2"));
        assertTrue(response.toString().contains("total=21"));
    }

    @Test
    void shouldExposePersistenceBaseFields() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 10, 0);
        LocalDateTime updatedAt = createdAt.plusMinutes(1);
        BasePO model = new BasePO(1L, false, createdAt, updatedAt);
        BasePO same = BasePO.builder()
                .id(1L)
                .deleted(false)
                .createTime(createdAt)
                .updateTime(updatedAt)
                .build();

        assertEquals(same, model);
        assertEquals(1L, model.getId());
        assertEquals(updatedAt, model.getUpdateTime());
        assertTrue(model.toString().contains("deleted=false"));
    }
}
