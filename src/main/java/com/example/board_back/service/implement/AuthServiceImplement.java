package com.example.board_back.service.implement;

import com.example.board_back.dto.request.auth.SignInRequestDto;
import com.example.board_back.dto.request.auth.SignUpRequestDto;
import com.example.board_back.dto.response.ResponseDto;
import com.example.board_back.dto.response.auth.SignInResponseDto;
import com.example.board_back.dto.response.auth.SignUpResponseDto;
import com.example.board_back.entity.UserEntity;
import com.example.board_back.provider.JwtProvider;
import com.example.board_back.repository.UserRepository;
import com.example.board_back.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImplement implements AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto) {

        try {

            String email = dto.getEmail();
            boolean existedEmail = userRepository.existsByEmail(email);

            if (existedEmail) {
                return SignUpResponseDto.duplicateEmail();
            }

            String nickname = dto.getNickname();
            boolean existedNickname = userRepository.existsByNickname(nickname);

            if (existedNickname) {
                return SignUpResponseDto.duplicateNickname();
            }

            String telNumber = dto.getTelNumber();
            boolean existedTelNumber = userRepository.existsByTelNumber(telNumber);

            if (existedTelNumber) {
                return SignUpResponseDto.duplicateTelNumber();
            }

            String password = dto.getPassword();
            String encryptedPassword = passwordEncoder.encode(password);
            dto.setPassword(encryptedPassword);

            UserEntity userEntity = new UserEntity(dto);
            userRepository.save(userEntity);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }

        return SignUpResponseDto.success();
    }

    @Override
    public ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto) {

        String token = null;

        try {

            String email = dto.getEmail();
            UserEntity userEntity = userRepository.findByEmail(email);

            if (userEntity == null) {
                return SignInResponseDto.signInFailed();
            }

            String password = dto.getPassword();
            String encodedPassword = userEntity.getPassword();
            boolean isMatched = passwordEncoder.matches(password, encodedPassword);

            if (!isMatched) {
                return SignInResponseDto.signInFailed();
            }

            token = jwtProvider.create(email);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }

        return SignInResponseDto.success(token);
    }
}
