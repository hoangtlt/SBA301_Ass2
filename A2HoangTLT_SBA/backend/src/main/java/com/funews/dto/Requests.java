/**
 * @author HoangTLT
 */

package com.funews.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

// Each record describes the fields accepted by one form/API request.
public class Requests {
    public record Login(@NotBlank @Email @Size(max = 255) String email,
                        @NotBlank @Size(max = 72) String password) { }
    public record Account(@NotBlank @Size(max = 100) String accountName,
                          @NotBlank @Email @Size(max = 255) String accountEmail,
                          @NotNull @Min(1) @Max(2) Integer accountRole,
                          @Size(max = 72) String password) { }
    public record Profile(@NotBlank @Size(max = 100) String accountName,
                          @NotBlank @Email @Size(max = 255) String accountEmail,
                          @NotBlank @Size(max = 72) String currentPassword,
                          @Size(max = 72) String newPassword) { }
    public record Category(@NotBlank @Size(max = 100) String categoryName,
                           @NotBlank @Size(max = 500) String categoryDescription,
                           @Positive Integer parentCategoryId,
                           @NotNull Boolean isActive) { }
    public record Tag(@NotBlank @Size(max = 50) String tagName,
                      @Size(max = 250) String note) { }
    public record News(@NotBlank @Size(max = 200) String newsTitle,
                       @NotBlank @Size(max = 300) String headline,
                       @NotBlank @Size(max = 20000) String newsContent,
                       @Size(max = 300) String newsSource,
                       @NotNull @Positive Integer categoryId,
                       @NotNull @Min(0) @Max(1) Integer newsStatus,
                       @NotNull @Size(max = 20) List<@NotNull @Valid Tag> tags) { }
}
