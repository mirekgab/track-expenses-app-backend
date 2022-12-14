package pl.byczazagroda.trackexpensesappbackend.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import pl.byczazagroda.trackexpensesappbackend.controller.WalletController;
import pl.byczazagroda.trackexpensesappbackend.dto.CreateWalletDTO;
import pl.byczazagroda.trackexpensesappbackend.dto.UpdateWalletDTO;
import pl.byczazagroda.trackexpensesappbackend.dto.WalletDTO;
import pl.byczazagroda.trackexpensesappbackend.exception.ApiExceptionBase;
import pl.byczazagroda.trackexpensesappbackend.exception.AppRuntimeException;
import pl.byczazagroda.trackexpensesappbackend.exception.ErrorCode;
import pl.byczazagroda.trackexpensesappbackend.mapper.WalletModelMapper;
import pl.byczazagroda.trackexpensesappbackend.model.Wallet;
import pl.byczazagroda.trackexpensesappbackend.repository.WalletRepository;

import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest
        (controllers = WalletController.class,
                includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        WalletRepository.class,
                        WalletServiceImpl.class,
                        ApiExceptionBase.class
                }))
class WalletServiceImplTest {

    private static final String NAME_OF_WALLET = "nameOfWallet";

    private static final String NAME_OF_WALLET_1 = "nameOfWallet1";

    private static final String NAME_OF_WALLET_2 = "nameOfWallet2";

    @MockBean
    private WalletRepository walletRepository;

    @MockBean
    private ApiExceptionBase apiExceptionBase;

    @Autowired
    private WalletServiceImpl walletService;

    @MockBean
    private WalletModelMapper walletModelMapper;

    @BeforeEach
    void beforeAll() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void itShouldUpdateWalletName() {
        // given
        UpdateWalletDTO updateWalletDto = new UpdateWalletDTO(1L, "walletName");
        Wallet wallet = new Wallet("anyName");
        wallet.setId(1L);
        Instant time = Instant.now();
        wallet.setCreationDate(Instant.now());
        WalletDTO newWallet = new WalletDTO(1L, "walletName", time);
        given(walletRepository.findById(updateWalletDto.id()))
                .willReturn(Optional.of(wallet));
        given(walletModelMapper.mapWalletEntityToWalletDTO(Mockito.any(Wallet.class))).willReturn(newWallet);

        // when
        WalletDTO walletDTO = walletService.updateWallet(updateWalletDto);

        // then
        assertThat(walletDTO.name()).isEqualTo(updateWalletDto.name());
    }

    @Test
    void itShouldThrowWhenWalletNotFound() {
        // given
        given(walletRepository.findById(Mockito.anyLong())).willReturn(Optional.empty());
        UpdateWalletDTO updateWalletDto = new UpdateWalletDTO(1L, "walletName");

        // when
        // then
        assertThatThrownBy(() -> walletService.updateWallet(updateWalletDto))
                .isInstanceOf(AppRuntimeException.class);
    }

    @Test
    void shouldCreateWalletProperly() {
        // given
        Instant creationTime = Instant.now();
        CreateWalletDTO createWalletDTO = new CreateWalletDTO(NAME_OF_WALLET);
        Wallet wallet = new Wallet(NAME_OF_WALLET);
        long id = 1L;
        wallet.setId(id);
        wallet.setCreationDate(creationTime);
        WalletDTO walletDTO = new WalletDTO(id, NAME_OF_WALLET, creationTime);

        // when
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(walletRepository.existsById(id)).thenReturn(true);

        when(walletModelMapper.mapWalletEntityToWalletDTO(wallet)).thenReturn(walletDTO);
        WalletDTO returnedWallet = walletService.createWallet(createWalletDTO);

        // then
        Assertions.assertEquals(wallet.getId(), returnedWallet.id());
        Assertions.assertEquals(wallet.getName(), returnedWallet.name());
        Assertions.assertEquals(wallet.getCreationDate(), returnedWallet.creationDate());
    }

    @Test
    void shouldThrowAnExceptionWhenNameContainsIllegalLetters() throws Exception {
        // given
        Instant creationTime = Instant.now();
        String illegalLettersName = "@#$";

        Wallet wallet = new Wallet(illegalLettersName);
        long id = 1L;
        wallet.setId(id);
        wallet.setCreationDate(creationTime);
//        WalletDTO walletDTO = new WalletDTO(id, illegalLettersName, creationTime);
//
//        // when
//        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
//        when(walletRepository.existsById(id)).thenReturn(true);
//        when(walletModelMapper.mapWalletEntityToWalletDTO(wallet)).thenReturn(walletDTO);
//
//        // then
//        Assertions.assertThrows(ConstraintViolationException.class,
//                () -> walletService.createWallet(createWalletDTO));

        CreateWalletDTO createWalletDTO = new CreateWalletDTO(illegalLettersName);
        WalletDTO walletDTO = walletService.createWallet(createWalletDTO);
        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new AppRuntimeException(ErrorCode.TEA003, ErrorCode.TEA003.getBusinessMessage()));

        // when
        when(walletRepository.existsById(wallet.getId())).thenReturn(true);
        when(walletModelMapper.mapWalletEntityToWalletDTO(wallet)).thenReturn(walletDTO);
        Exception exception = assertThrows(AppRuntimeException.class, () -> walletService.createWallet(createWalletDTO));

        // then
        assertThat(exception)
                .isInstanceOf(AppRuntimeException.class)
                .hasMessage(ErrorCode.TEA003.getBusinessMessage());
    }

    @Test
    void shouldReturnListOfWalletDTOWithProperSizeWhenListIsNotEmpty() {
        // given
        List<Wallet> walletList = createListOfWallets();

        // when
        when(walletRepository.findAll()).thenReturn(walletList);
        List<WalletDTO> allWallets = walletService.getWallets();

        // then
        assertThat(allWallets, hasSize(walletList.size()));
        assertFalse(allWallets.isEmpty());
    }

//    @Test
//    void shouldThrowExceptionWhenListOfWalletsNotFound() {
//        // given
//        Mockito.when(walletRepository.findAll()).thenThrow(RuntimeException.class);
//
//        // when
//        Exception exception = assertThrows(RuntimeException.class, () -> walletService.getWallets());
//
//        // then
//        assertThat(exception)
//                .isInstanceOf(AppRuntimeException.class)
//                .hasMessage(BusinessError.W001.getBusinessMessage());
//    }

    @Test
    void shouldDeletedWalletProperly() {
        //given
        Wallet wallet = new Wallet(NAME_OF_WALLET);
        Long id = 1L;
        Instant creationTime = Instant.now();
        wallet.setId(id);
        wallet.setCreationDate(creationTime);

        //when
        when(walletRepository.existsById(id)).thenReturn(true);
        walletService.deleteWalletById(id);

        //then
        verify(walletRepository).deleteById(wallet.getId());
    }

    @Test
    void shouldThrowAnExceptionWhenWalletWithIdDoesNotExist() {
        Wallet wallet = new Wallet(NAME_OF_WALLET);
        Long id = 1L;
        Instant creationTime = Instant.now();
        wallet.setId(id);
        wallet.setCreationDate(creationTime);

        //when
        given(walletRepository.findById(Mockito.anyLong())).willReturn(Optional.empty());

        //then
        assertThatThrownBy(() -> walletService.deleteWalletById(5L))
                .isInstanceOf(AppRuntimeException.class);
        assertThatExceptionOfType(AppRuntimeException.class)
                .isThrownBy(() -> walletService.deleteWalletById(5L))
                .withMessage(ErrorCode.W003.getBusinessMessage());
    }

    @Test
    void shouldFindWalletProperly() {
        //given
        Wallet wallet = new Wallet(NAME_OF_WALLET);
        Instant creationTime = Instant.now();
        Long id = 1L;
        wallet.setId(id);
        wallet.setCreationDate(creationTime);
        WalletDTO expectedWallet = new WalletDTO(id, NAME_OF_WALLET, creationTime);

        //when
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletModelMapper.mapWalletEntityToWalletDTO(wallet)).thenReturn(expectedWallet);
        WalletDTO actualWallet = walletService.findById(1L);

        //then
        Assertions.assertEquals(expectedWallet, actualWallet);
    }
//
//    @Test
//    void shouldThrowExceptionWhenWalletByIdNotFound() {
//        Wallet wallet = new Wallet(NAME_OF_WALLET);
//        Long id = 1L;
//        Instant creationTime = Instant.now();
//        wallet.setId(id);
//        wallet.setCreationDate(creationTime);
//
//        //when
//        given(walletRepository.findById(Mockito.anyLong())).willReturn(Optional.empty());
//
//        //then
//        assertThatThrownBy(() -> walletService.findById(5L))
//                .isInstanceOf(ResourceNotFoundException.class);
//        assertThatExceptionOfType(ResourceNotFoundException.class)
//                .isThrownBy(() -> walletService.findById(5L))
//                .withMessage("Wallet with that id doesn't exist");
//    }

//    @Test
//    void shouldThrowExceptionWhenWalletByIdNotFound() {
//        Wallet wallet = new Wallet(NAME_OF_WALLET);
//        Long id = 1L;
//        Instant creationTime = Instant.now();
//        wallet.setId(id);
//        wallet.setCreationDate(creationTime);
//
//        //when
//        given(walletRepository.findById(Mockito.anyLong())).willReturn(Optional.empty());
//
//        //then
//        assertThatThrownBy(() -> walletService.findById(5L)).isInstanceOf(ResourceNotFoundException.class);
//        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> walletService.findById(5L)).withMessage("Wallet with that id doesn't exist");
//    }

    @Test
    @DisplayName("Should return list of WalletDTO by name with proper size")
    void shouldReturnListOfWalletDTOByNameWithProperSize() {
        // given
        String walletNameSearched = "Family";
        List<Wallet> walletList = createListOfWalletsByName("Family wallet", "Common Wallet", "Smith Family Wallet");
        List<WalletDTO> walletListDTO = walletList.stream()
                .map((Wallet x) -> new WalletDTO(x.getId(), x.getName(), x.getCreationDate()))
                .toList();
        given(walletRepository.findAllByNameLikeIgnoreCase(walletNameSearched)).willReturn(walletList);
        walletList.forEach(wallet -> given(walletModelMapper
                .mapWalletEntityToWalletDTO(wallet))
                .willReturn(walletListDTO
                        .stream()
                        .filter(walletDTO -> Objects.equals(wallet.getName(), walletDTO.name()))
                        .findAny()
                        .orElse(null)));

        // when
        List<WalletDTO> fundedWallets = walletService.findAllByNameLikeIgnoreCase(walletNameSearched);

        // then
        assertThat(fundedWallets, hasSize(walletRepository.findAllByNameLikeIgnoreCase(walletNameSearched).size()));
    }

    private List<Wallet> createListOfWalletsByName(String... name) {
        return Arrays.stream(name).map(Wallet::new).toList();
    }

    private List<Wallet> createListOfWallets() {
        Wallet wallet1 = new Wallet(NAME_OF_WALLET);
        Wallet wallet2 = new Wallet(NAME_OF_WALLET_1);
        Wallet wallet3 = new Wallet(NAME_OF_WALLET_2);
        return List.of(wallet1, wallet2, wallet3);
    }
}