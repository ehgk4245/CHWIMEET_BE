package com.back.domain.report.controller;

import com.back.BaseContainerIntegrationTest;
import com.back.domain.report.repository.ReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/report/report_admin.sql")
class ReportAdminControllerTest extends BaseContainerIntegrationTest {

    @Autowired ReportRepository reportRepository;

    @Test
    @DisplayName("관리자 - 모든 신고 목록 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getReports_AllTypes_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/adm/reports"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.status").value(200),
                        jsonPath("$.msg").value("OK"),
                        jsonPath("$.data.content").isArray(),
                        jsonPath("$.data.content.length()").value(9),
                        jsonPath("$.data.content[0].reportType").value("REVIEW"),
                        jsonPath("$.data.page.page").value(0),
                        jsonPath("$.data.page.size").value(10),
                        jsonPath("$.data.page.totalElements").value(9),
                        jsonPath("$.data.page.totalPages").value(1)
                )
                .andDo(print());
    }

    @Test
    @DisplayName("관리자 - POST 타입만 필터링하여 조회")
    @WithMockUser(roles = "ADMIN")
    void getReports_FilterByPostType_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/adm/reports")
                        .param("reportType", "POST")
               )
               .andExpectAll(
                       status().isOk(),
                       jsonPath("$.data.content.length()").value(3),
                       jsonPath("$.data.content[0].reportType").value("POST"),
                       jsonPath("$.data.page.totalElements").value(3)
               )
                .andDo(print());
    }

    @Test
    @DisplayName("관리자 - MEMBER 타입만 필터링하여 조회")
    @WithMockUser(roles = "ADMIN")
    void getReports_FilterByMemberType_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/adm/reports")
                        .param("reportType", "MEMBER")
               )
               .andExpectAll(
                       status().isOk(),
                       jsonPath("$.data.content.length()").value(3),
                       jsonPath("$.data.content[0].reportType").value("MEMBER"),
                       jsonPath("$.data.page.totalElements").value(3)
               )
               .andDo(print());
    }

    @Test
    @DisplayName("관리자 - REVIEW 타입만 필터링하여 조회")
    @WithMockUser(roles = "ADMIN")
    void getReports_FilterByReviewType_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/adm/reports")
                       .param("reportType", "REVIEW")
               )
               .andExpectAll(
                       status().isOk(),
                       jsonPath("$.data.content.length()").value(3),
                       jsonPath("$.data.content[0].reportType").value("REVIEW"),
                       jsonPath("$.data.page.totalElements").value(3)
               )
               .andDo(print());
    }

    @Test
    @DisplayName("관리자 - 페이징 테스트 (첫 페이지)")
    @WithMockUser(roles = "ADMIN")
    void getReports_Pagination_FirstPage() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/adm/reports")
                        .param("page", "0")
                        .param("size", "2")
               )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.data.content.length()").value(2),
                        jsonPath("$.data.page.page").value(0),
                        jsonPath("$.data.page.size").value(2),
                        jsonPath("$.data.page.totalElements").value(9),
                        jsonPath("$.data.page.totalPages").value(5),
                        jsonPath("$.data.page.first").value(true),
                        jsonPath("$.data.page.last").value(false),
                        jsonPath("$.data.page.hasNext").value(true),
                        jsonPath("$.data.page.hasPrevious").value(false)
                )
                .andDo(print());
    }

    @Test
    @DisplayName("관리자 - 페이징 테스트 (두 번째 페이지)")
    @WithMockUser(roles = "ADMIN")
    void getReports_Pagination_SecondPage() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/adm/reports")
                        .param("page", "1")
                        .param("size", "2")
               )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.data.page.page").value(1),
                        jsonPath("$.data.page.first").value(false),
                        jsonPath("$.data.page.last").value(false),
                        jsonPath("$.data.page.hasNext").value(true),
                        jsonPath("$.data.page.hasPrevious").value(true)
                )
                .andDo(print());
    }

    @Test
    @DisplayName("관리자 - 빈 결과 조회")
    @WithMockUser(roles = "ADMIN")
    void getReports_EmptyResult() throws Exception {
        // given
        reportRepository.deleteAllInBatch();

        // when & then
        mockMvc.perform(get("/api/v1/adm/reports"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.data.content").isEmpty(),
                        jsonPath("$.data.page.totalElements").value(0),
                        jsonPath("$.data.page.totalPages").value(0)
                )
                .andDo(print());
    }

    @Test
    @DisplayName("관리자 - 정렬 파라미터 테스트")
    @WithMockUser(roles = "ADMIN")
    void getReports_WithSortParameter() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/adm/reports")
                       .param("sort", "createdAt,asc")
               )
               .andExpectAll(
                       status().isOk(),
                       jsonPath("$.data.page.sort[0].property").value("createdAt"),
                       jsonPath("$.data.page.sort[0].direction").value("ASC")
               )
               .andDo(print());
    }

    @Test
    @DisplayName("일반 사용자 - 접근 거부")
    @WithMockUser
    void getReports_Forbidden_NormalUser() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/adm/reports"))
               .andExpectAll(
                       status().isForbidden(),
                       jsonPath("$.status").value(403),
                       jsonPath("$.msg").value("권한이 없습니다.")
               )
               .andDo(print());
    }

    @Test
    @DisplayName("인증되지 않은 사용자 - 접근 거부")
    @WithAnonymousUser
    void getReports_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/adm/reports"))
               .andExpectAll(
                       status().isUnauthorized(),
                       jsonPath("$.status").value(401),
                       jsonPath("$.msg").value("로그인 후 이용해주세요.")
               )
               .andDo(print());
    }

    @Test
    @DisplayName("관리자 - 잘못된 reportType 파라미터")
    @WithMockUser(roles = "ADMIN")
    void getReports_InvalidReportType() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/adm/reports")
                        .param("reportType", "INVALID_TYPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("관리자 - 여러 필터와 페이징 조합")
    @WithMockUser(roles = "ADMIN")
    void getReports_CombinedFiltersAndPaging() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/adm/reports")
                       .param("reportType", "POST")
                       .param("page", "2")
                       .param("size", "5")
                       .param("sort", "id,desc")
               )
               .andExpectAll(
                       status().isOk(),
                       jsonPath("$.data.page.page").value(2),
                       jsonPath("$.data.page.size").value(5),
                       jsonPath("$.data.page.last").value(true),
                       jsonPath("$.data.page.sort[0].property").value("id"),
                       jsonPath("$.data.page.sort[0].direction").value("DESC")
               )
               .andDo(print());
    }
}