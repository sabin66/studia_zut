import pytest
import math
from kalkulator import Calc

class TestCalcBasicOperations:
    @pytest.mark.parametrize('a,b',[
        (1,2),
        (10,10),
        (45,77)
    ])
    def test_add_two_numbers(self,a,b):
        calc = Calc()
        result = calc.add(a, b)
        assert result == a+b
        assert calc.val == a+b
    
    @pytest.mark.parametrize('a,b',[
        (1,2),
        (10,10)
    ])
    def test_add_uses_current_value_when_a_is_none(self,a,b):
        calc = Calc()
        calc.val = a
        result = calc.add(b)
        assert result == a+b
        assert calc.val == a+b

    @pytest.mark.parametrize('a,b',[
        (1,2),
        (10,10)
    ])
    def test_subtract_two_numbers(self,a,b):
        calc = Calc()
        result = calc.subtract(b, a)
        assert result == a-b
        assert calc.val == a-b
    
    @pytest.mark.parametrize('a,b',[
        (1,2),
        (10,10)
    ])
    def test_subtract_uses_current_value(self,a,b):
        calc = Calc()
        calc.val = a
        result = calc.subtract(b)
        assert result == a-b
    
    @pytest.mark.parametrize('a,b',[
        (1,2),
        (10,10)
    ])
    def test_multiply_two_numbers(self,a,b):
        calc = Calc()
        result = calc.multiply(a, b)
        assert result == a*b
        assert calc.val == a*b
    
    @pytest.mark.parametrize('a,b',[
        (1,2),
        (10,10)
    ])
    def test_multiply_uses_current_value(self,a,b):
        calc = Calc()
        calc.val = a
        result = calc.multiply(b)
        assert result == a*b
    
    @pytest.mark.parametrize('a,b',[
        (1,2),
        (10,10)
    ])
    def test_divide_two_numbers(self,a,b):
        calc = Calc()
        result = calc.divide(b, a)
        assert result == a/b
        assert calc.val == a/b
    
    def test_divide_by_zero_returns_error(self):
        calc = Calc()
        result = calc.divide(0, 10)
        assert result == "ZeroDivisionError"
    
    @pytest.mark.parametrize('a,b',[
        (1,2),
        (10,10)
    ])
    def test_divide_uses_current_value(self,a,b):
        calc = Calc()
        calc.val = a
        result = calc.divide(b)
        assert result == a/b


class TestCalcChainedOperations:
    
    def test_chain_add_and_multiply(self):
        calc = Calc()
        calc.add(5, 3)  # 8
        calc.multiply(2)  # 16
        assert calc.val == 16
    
    def test_chain_subtract_divide_add(self):
        calc = Calc()
        calc.subtract(5, 20)  # 15
        calc.divide(3)  # 5
        calc.add(10)  # 15
        assert calc.val == 15


class TestCalcAdvancedOperations:
    
    def test_modulo_operation(self):
        calc = Calc()
        result = calc.modulo(3, 10)
        assert result == 1
        assert calc.val == 1
    
    def test_modulo_uses_current_value(self):
        calc = Calc()
        calc.val = 17
        result = calc.modulo(5)
        assert result == 2
    
    def test_power_operation(self):
        calc = Calc()
        result = calc.power(3, 2)
        assert result == 8.0
        assert calc.val == 8.0
    
    def test_power_uses_current_value(self):
        calc = Calc()
        calc.val = 5
        result = calc.power(2)
        assert result == 25.0
    
    def test_sqrt_of_positive_number(self):
        calc = Calc()
        result = calc.sqrt(16)
        assert result == 4.0
        assert calc.val == 4.0
    
    def test_sqrt_uses_current_value(self):
        calc = Calc()
        calc.val = 25
        result = calc.sqrt()
        assert result == 5.0
    
    def test_sqrt_of_negative_returns_error(self):
        calc = Calc()
        result = calc.sqrt(-4)
        assert result == "ValueError: Negative Sqrt"
    
    def test_inverse_of_number(self):
        calc = Calc()
        result = calc.inverse(4)
        assert result == 0.25
        assert calc.val == 0.25
    
    def test_inverse_of_zero_returns_error(self):
        calc = Calc()
        result = calc.inverse(0)
        assert result == "ZeroDivisionError"
    
    def test_percent_calculation(self):
        calc = Calc()
        result = calc.percent(20, 50)  # 20% of 50
        assert result == 10.0
        assert calc.val == 10.0
    
    def test_negate_positive_number(self):
        calc = Calc()
        result = calc.negate(5)
        assert result == -5
        assert calc.val == -5
    
    def test_negate_negative_number(self):
        calc = Calc()
        result = calc.negate(-10)
        assert result == 10
        assert calc.val == 10


class TestCalcMemoryOperations:
    
    def test_memory_save_with_value(self):
        calc = Calc()
        result = calc.memory_save(42)
        assert result == "Saved: 42"
        assert 42 in calc.mem
    
    def test_memory_save_uses_current_value(self):
        calc = Calc()
        calc.val = 100
        calc.memory_save()
        assert 100 in calc.mem
    
    def test_memory_recall_returns_last_saved(self):
        calc = Calc()
        calc.memory_save(10)
        calc.memory_save(20)
        result = calc.memory_recall()
        assert result == 20
        assert calc.val == 20
    
    def test_memory_recall_empty_returns_zero(self):
        calc = Calc()
        result = calc.memory_recall()
        assert result == 0
        assert calc.val == 0
    
    def test_memory_clear_all(self):
        calc = Calc()
        calc.memory_save(10)
        calc.memory_save(20)
        result = calc.memory_clear_all()
        assert result == "Memory Cleared"
        assert calc.mem == []
    
    def test_memory_add_to_existing(self):
        calc = Calc()
        calc.memory_save(10)
        result = calc.memory_add(5)
        assert result == "Mem: 15"
        assert calc.mem[-1] == 15
    
    def test_memory_add_creates_zero_if_empty(self):
        calc = Calc()
        calc.memory_add(5)
        assert calc.mem[-1] == 5
    
    def test_memory_sub_from_existing(self):
        calc = Calc()
        calc.memory_save(20)
        result = calc.memory_sub(7)
        assert result == "Mem: 13"
        assert calc.mem[-1] == 13
    
    def test_memory_sub_creates_zero_if_empty(self):
        calc = Calc()
        calc.memory_sub(5)
        assert calc.mem[-1] == -5


class TestCalcClearOperations:
    
    def test_clear_resets_value_to_zero(self):
        calc = Calc()
        calc.val = 42
        result = calc.clear()
        assert result == 0
        assert calc.val == 0
    
    def test_clear_keeps_memory(self):
        calc = Calc()
        calc.memory_save(10)
        calc.clear()
        assert len(calc.mem) == 1
        assert calc.mem[0] == 10
    
    def test_all_clear_resets_everything(self):
        calc = Calc()
        calc.val = 42
        calc.memory_save(10)
        result = calc.all_clear()
        assert result == 0
        assert calc.val == 0
        assert calc.mem == []


class TestCalcDeveloperMode:
    
    def test_add_developer_binary_strings(self):
        calc = Calc()
        calc.set_base('BIN')
        result = calc.add_developer('101', '11')  # 5 + 3 = 8
        assert result == '1000'
        assert calc.val == 8
    
    def test_add_developer_with_overflow(self):
        calc = Calc()
        calc.set_base('BIN')  # <--- Kontekst Binarny
        calc.set_bit_mode('BYTE')
        result = calc.add_developer('11111111', '1')  # 255 + 1 = 256, masked to 0
        assert result == '0'
    
    def test_subtract_developer_binary_strings(self):
        calc = Calc()
        calc.set_base('BIN')
        result = calc.subtract_developer('1010', '11')  # 10 - 3 = 7
        assert result == '111'
        assert calc.val == 7
    
    def test_multiply_developer_binary_strings(self):
        calc = Calc()
        calc.set_base('BIN')
        result = calc.multiply_developer('101', '11')  # 5 * 3 = 15
        assert result == '1111'
        assert calc.val == 15
    
    def test_divide_developer_binary_strings(self):
        calc = Calc()
        calc.set_base('BIN')
        result = calc.divide_developer('1010', '10')  # 10 / 2 = 5
        assert result == '101'
        assert calc.val == 5
    
    def test_divide_developer_by_zero(self):
        calc = Calc()
        calc.set_base('BIN')
        result = calc.divide_developer('1010', '0')
        assert result == "ZeroDivisionError"
    
    def test_and_developer_operation(self):
        calc = Calc()
        calc.set_base('BIN')
        result = calc.and_developer('1100', '1010')  # 12 & 10 = 8
        assert result == '1000'
        assert calc.val == 8
    
    def test_or_developer_operation(self):
        calc = Calc()
        calc.set_base('BIN')
        result = calc.or_developer('1100', '1010')  # 12 | 10 = 14
        assert result == '1110'
        assert calc.val == 14
    
    def test_xor_developer_operation(self):
        calc = Calc()
        calc.set_base('BIN')
        result = calc.xor_developer('1100', '1010')  # 12 ^ 10 = 6
        assert result == '110'
        assert calc.val == 6
    
    def test_not_developer_operation(self):
        calc = Calc()
        calc.set_base('BIN')
        calc.set_bit_mode('BYTE')
        result = calc.not_developer('1010')  # ~10 = -11, masked to 245
        assert result == '11110101'
        assert calc.val == 245
    
    def test_lsh_developer_left_shift(self):
        calc = Calc()
        calc.set_base('BIN')
        result = calc.lsh_developer('101', '10')  # 5 << 2 = 20
        assert result == '10100'
        assert calc.val == 20
    
    def test_rsh_developer_right_shift(self):
        calc = Calc()
        calc.set_base('BIN')
        result = calc.rsh_developer('10100', '10')  # 20 >> 2 = 5
        assert result == '101'
        assert calc.val == 5
    
    def test_developer_operations_use_current_value(self):
        calc = Calc()
        calc.set_base('BIN')
        calc.val = 5
        result = calc.add_developer(None, '11')  # 5 + 3 = 8
        assert result == '1000'
        assert calc.val == 8

class TestCalcBinaryStringInput:
    
    def test_get_a_converts_binary_string(self):
        calc = Calc()
        result = calc.add(5, '0b101')
        assert result == 10
    
    def test_get_a_converts_numeric_string(self):
        calc = Calc()
        result = calc.add(5, '10.5')
        assert result == 15.5


class TestCalcEdgeCases:
    
    def test_operations_with_zero(self):
        calc = Calc()
        assert calc.add(0, 5) == 5
        assert calc.multiply(0, 5) == 0
        assert calc.subtract(0, 5) == 5
    
    def test_operations_with_negative_numbers(self):
        calc = Calc()
        assert calc.add(5, -3) == 2
        assert calc.multiply(2, -4) == -8
        assert calc.subtract(3, -5) == -8
    
    def test_operations_with_floats(self):
        calc = Calc()
        result = calc.add(2.5, 3.7)
        assert abs(result - 6.2) < 0.0001
    
    def test_power_with_zero_exponent(self):
        calc = Calc()
        result = calc.power(0, 5)
        assert result == 1.0
    
    def test_power_with_negative_exponent(self):
        calc = Calc()
        result = calc.power(-2, 2)
        assert result == 0.25
    
    def test_sqrt_of_zero(self):
        calc = Calc()
        result = calc.sqrt(0)
        assert result == 0.0
    
    def test_percent_of_zero(self):
        calc = Calc()
        result = calc.percent(50, 0)
        assert result == 0.0


class TestCalcInitialization:
    
    def test_new_calculator_has_zero_value(self):
        calc = Calc()
        assert calc.val == 0
    
    def test_new_calculator_has_empty_memory(self):
        calc = Calc()
        assert calc.mem == []
    
    def test_multiple_calculators_are_independent(self):
        calc1 = Calc()
        calc2 = Calc()
        calc1.val = 10
        calc2.val = 20
        assert calc1.val == 10
        assert calc2.val == 20

class TestCalcBitwiseModes:
    
    def test_initial_mode_is_qword(self):
        calc = Calc()
        assert calc.bit_mode == "QWORD"
        assert calc.mask == 0xFFFFFFFFFFFFFFFF

    def test_switch_modes_updates_mask(self):
        calc = Calc()
        
        calc.set_bit_mode("DWORD")
        assert calc.mask == 0xFFFFFFFF
        
        calc.set_bit_mode("WORD")
        assert calc.mask == 0xFFFF
        
        calc.set_bit_mode("BYTE")
        assert calc.mask == 0xFF

    def test_byte_mode_overflow(self):
        calc = Calc()
        calc.set_base("BIN")
        calc.set_bit_mode("BYTE")
        
        result = calc.add_developer('11111111', '1')
        
        assert result == '0'
        assert calc.val == 0

    def test_word_mode_overflow(self):
        calc = Calc()
        calc.set_bit_mode("WORD")
        calc.set_base("DEC")
        
        calc.val = 65535
        calc.add_developer(None, '1')
        
        assert calc.val == 0

    def test_clamping_when_switching_modes_down(self):
        calc = Calc()
        calc.val = 257
        
        calc.set_bit_mode("BYTE")
        
        assert calc.val == 1

    def test_negative_numbers_representation_byte(self):
        calc = Calc()
        calc.set_bit_mode("BYTE")
        calc.set_base('BIN')
        
        # 0 - 1 = -1. W 8 bitach -1 to 11111111 (255)
        calc.subtract_developer('0', '1')
        
        assert calc.val == 255
        assert bin(calc.val) == '0b11111111'

    def test_not_operation_differs_by_mode(self):
        calc = Calc()
        calc.val = 0
        calc.set_base('DEC')
        calc.set_bit_mode("BYTE")
        res_byte = calc.not_developer(None)
        assert calc.val == 255
        
        calc.val = 0
        calc.set_bit_mode("WORD")
        res_word = calc.not_developer(None)
        assert calc.val == 65535

    def test_shift_left_overflow_byte(self):
        calc = Calc()
        calc.set_bit_mode("BYTE")
        calc.set_base("BIN")
        
        calc.lsh_developer('10000000', '1')
        
        assert calc.val == 0

    def test_dword_mode_operations(self):
        calc = Calc()
        calc.set_bit_mode("DWORD")
        calc.set_base("BIN")
        
        calc.val = 4294967295
        
        # Dodanie 5 powinno przekręcić licznik: 4
        calc.add_developer(None, '101') # binary 5
        
        assert calc.val == 4

    def test_invalid_mode_does_not_change_state(self):
        calc = Calc()
        original_mask = calc.mask
        
        msg = calc.set_bit_mode("SUPER_COMPUTER_MODE")
        
        assert msg == "Invalid Mode"
        assert calc.mask == original_mask


class TestCalcHexBaseModes:

    def test_initial_base_is_decimal(self):
        calc = Calc()
        assert calc.base == 10

    def test_change_base_updates_state(self):
        calc = Calc()
        calc.set_base('HEX')
        assert calc.base == 16
        
        calc.set_base('BIN')
        assert calc.base == 2
        
        calc.set_base('OCT')
        assert calc.base == 8

    def test_input_parsing_respects_base_hex(self):
        calc = Calc()
        calc.set_base('HEX')

        result = calc.add_developer('A', '1')
        assert result == 'B'
        assert calc.val == 11
        
        calc.clear()
        calc.add_developer('10', '0')
        assert calc.val == 16

    def test_input_parsing_respects_base_bin(self):
        calc = Calc()
        calc.set_base('BIN')

        result = calc.add_developer('10', '1')
        assert result == '11'
        assert calc.val == 3

    def test_explicit_prefixes_override_base(self):
        calc = Calc()
        calc.set_base('DEC')

        result = calc.add_developer('0xFF', '0b1')
        
        assert result == '256'
        assert calc.val == 256

    def test_output_formatting_hex(self):
        calc = Calc()
        calc.set_base('HEX')

        calc.val = 255
        result = calc.add_developer(None, '0')
        
        assert result == 'FF'

    def test_output_formatting_oct(self):
        calc = Calc()
        calc.set_base('OCT')

        calc.val = 8
        result = calc.add_developer(None, '0')
        
        assert result == '10'

    def test_mixed_base_calculations(self):
        calc = Calc()
        calc.set_base('HEX')

        result = calc.add_developer('A', '0b101')
        
        assert result == 'F'
        assert calc.val == 15

    def test_byte_overflow_in_hex_mode(self):
        calc = Calc()
        calc.set_base('HEX')
        calc.set_bit_mode('BYTE')
        
        result = calc.add_developer('FF', '1')
        
        assert result == '0'
        assert calc.val == 0

    def test_negative_numbers_hex_display(self):
        calc = Calc()
        calc.set_bit_mode('BYTE')
        calc.set_base('HEX')
        
        result = calc.subtract_developer('0', '1')
        
        assert result == 'FF'

if __name__ == "__main__":
    pytest.main([__file__, "-v"])