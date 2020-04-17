package moon.odyssey.webflux.utils.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AppErrorResponse {

    private Long timestamp;

    private String path;

    private Integer status;

    private String error;

    private String message;

    private String requestId;

}
