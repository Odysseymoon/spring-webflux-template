package moon.odyssey.webflux.application.auth.model;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserParam {

    @NotEmpty
    @Length(min = 4, max = 30)
    private String userId;

    @NotEmpty
    @Length(min = 8, max = 50)
    private String password;
}
