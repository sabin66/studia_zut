import math
import os

class Calc:
    # Definicje masek bitowych
    MASKS = {
        "QWORD": 0xFFFFFFFFFFFFFFFF, # 64-bit
        "DWORD": 0xFFFFFFFF,         # 32-bit
        "WORD":  0xFFFF,             # 16-bit
        "BYTE":  0xFF                # 8-bit
    }

    def __init__(self):
        self.mem = []
        self.val: int = 0
        self.bit_mode = "QWORD"       
        self.mask = self.MASKS["QWORD"]
        self.base = 10  # Domyślna baza wejściowa

    def set_bit_mode(self, mode):
        mode = mode.upper()
        if mode in self.MASKS:
            self.bit_mode = mode
            self.mask = self.MASKS[mode]
            self.val = self.val & self.mask
            return f"Bit Mode: {mode}"
        return "Invalid Mode"

    def set_base(self, base_str):
        base_map = {'HEX': 16, 'DEC': 10, 'OCT': 8, 'BIN': 2}
        b = base_str.upper()
        if b in base_map:
            self.base = base_map[b]
            return f"Base changed to {b}"
        return "Invalid Base"

    def _update_state(self, result):
        self.val = result
        return result
    
    def _get_a(self, a):
        if a is None:
            return self.val
        if isinstance(a, str):
            try:
                if a.lower().startswith('0x'): return int(a, 16)
                if a.lower().startswith('0b'): return int(a, 2)
                return float(a)
            except ValueError:
                return float(a)
        return a

    def _parse_dev_input(self, val):
        if val is None:
            return self.val
        if isinstance(val, int) or isinstance(val, float):
            return int(val)
        if isinstance(val, str):
            val = val.strip()
            if val.lower().startswith('0x'): return int(val, 16)
            if val.lower().startswith('0b'): return int(val, 2)
            if val.lower().startswith('0o'): return int(val, 8)
            
            try:
                return int(val, self.base)
            except ValueError:
                return 0 # Lub rzuć błąd
        return int(val)

    def _format_result(self):
        val = self.val & self.mask
        if self.base == 16: return hex(val).upper().replace("0X", "")
        if self.base == 8:  return oct(val).replace("0o", "")
        if self.base == 2:  return bin(val).replace("0b", "")
        return str(val)
    
    def multiply_developer(self, a, b):
        val_a = self._parse_dev_input(a) if a is not None else self.val
        val_b = self._parse_dev_input(b)
        self.val = (val_a * val_b) & self.mask
        return self._format_result()
    
    def add_developer(self, a, b):
        val_a = self._parse_dev_input(a) if a is not None else self.val
        val_b = self._parse_dev_input(b)
        self.val = (val_a + val_b) & self.mask
        return self._format_result()
    
    def subtract_developer(self, a, b):
        val_a = self._parse_dev_input(a) if a is not None else self.val
        val_b = self._parse_dev_input(b)
        self.val = (val_a - val_b) & self.mask
        return self._format_result()
    
    def divide_developer(self, a, b):
        val_a = self._parse_dev_input(a) if a is not None else self.val
        val_b = self._parse_dev_input(b)
        if val_b == 0: return "ZeroDivisionError"
        self.val = int(val_a // val_b) & self.mask
        return self._format_result()

    def and_developer(self, a, b):
        val_a = self._parse_dev_input(a) if a is not None else self.val
        val_b = self._parse_dev_input(b)
        self.val = (val_a & val_b) & self.mask
        return self._format_result()

    def or_developer(self, a, b):
        val_a = self._parse_dev_input(a) if a is not None else self.val
        val_b = self._parse_dev_input(b)
        self.val = (val_a | val_b) & self.mask
        return self._format_result()

    def xor_developer(self, a, b):
        val_a = self._parse_dev_input(a) if a is not None else self.val
        val_b = self._parse_dev_input(b)
        self.val = (val_a ^ val_b) & self.mask
        return self._format_result()
    
    def not_developer(self, a):
        val_a = self._parse_dev_input(a) if a is not None else self.val
        self.val = (~val_a) & self.mask
        return self._format_result()

    def lsh_developer(self, a, b):
        val_a = self._parse_dev_input(a) if a is not None else self.val
        shift = self._parse_dev_input(b)
        self.val = (val_a << shift) & self.mask
        return self._format_result()

    def rsh_developer(self, a, b):
        val_a = self._parse_dev_input(a) if a is not None else self.val
        shift = self._parse_dev_input(b)
        self.val = (val_a >> shift) & self.mask
        return self._format_result()

    def multiply(self, b, a=None):
        a = self._get_a(a)
        return self._update_state(a * b)
    
    def add(self, b, a=None):
        a = self._get_a(a)
        return self._update_state(a + b)

    def subtract(self, b, a=None):
        a = self._get_a(a)
        return self._update_state(a - b)
    
    def divide(self, b, a=None):
        if b == 0: return "ZeroDivisionError"
        a = self._get_a(a)
        return self._update_state(a / b)
    
    def clear(self):
        self.val = 0
        return 0
    
    def all_clear(self):
        self.val = 0
        self.mem = []
        return 0

    def memory_save(self, a=None):
        val_to_save = a if a is not None else self.val
        self.mem.append(val_to_save)
        return f"Saved: {val_to_save}"

    def modulo(self, b, a=None):
        a = self._get_a(a)
        return self._update_state(a % b)

    def power(self, b, a=None):
        a = self._get_a(a)
        return self._update_state(math.pow(a, b))

    def sqrt(self, a=None):
        a = self._get_a(a)
        if a < 0: return "ValueError: Negative Sqrt"
        return self._update_state(math.sqrt(a))
    
    def inverse(self, a=None):
        a = self._get_a(a)
        if a == 0: return "ZeroDivisionError"
        return self._update_state(1 / a)
    
    def percent(self, b, a=None):
        a = self._get_a(a)
        sol = (a * b) / 100
        self.val = sol
        return sol
    
    def negate(self, a=None):
        a = self._get_a(a)
        return self._update_state(-a)

    def memory_recall(self):
        if not self.mem: return 0
        self.val = self.mem[-1]
        return self.val

    def memory_clear_all(self):
        self.mem = []
        return "Memory Cleared"
    
    def memory_add(self, val=None):
        v = val if val is not None else self.val
        if not self.mem: self.mem.append(0)
        self.mem[-1] += v
        return f"Mem: {self.mem[-1]}"

    def memory_sub(self, val=None):
        v = val if val is not None else self.val
        if not self.mem: self.mem.append(0)
        self.mem[-1] -= v
        return f"Mem: {self.mem[-1]}"

def cli_interface():
    calc = Calc()
    mode = "STD"
    
    while True:
        print("\n" + "="*60)
        if mode == "DEV":
            curr_val = int(calc.val) & calc.mask
            
            hex_mark = "<" if calc.base == 16 else " "
            dec_mark = "<" if calc.base == 10 else " "
            oct_mark = "<" if calc.base == 8 else " "
            bin_mark = "<" if calc.base == 2 else " "
            
            print(f"HEX: {hex(curr_val).replace('0x','').upper()} {hex_mark}")
            print(f"DEC: {curr_val} {dec_mark}")
            print(f"OCT: {oct(curr_val).replace('0o','')} {oct_mark}")
            print(f"BIN: {bin(curr_val).replace('0b','')} {bin_mark}")
            
            print("-" * 60)
            print(f"Mode: {calc.bit_mode} | Input Base: {calc.base}")
        else:
            print(f"WIN 10 CALCULATOR | Mode: {mode}")
            print(f"Display (Val): {calc.val}")
            
        print(f"Memory: {calc.mem}")
        print("-" * 60)
        
        if mode == "STD":
            print("Commands: +, -, *, /, %, ^, v, 1/x, inv")
            print("Control: c, dev, exit")
        else:
            print("Base:    hex, dec, oct, bin")
            print("Size:    qword, dword, word, byte")
            print("Bitwise: &, |, ^, ~, <<, >>")
            print("Math:    +, -, *, /")
            print("Control: c, std, exit")
        
        cmd = input(">>> ").strip().lower()
        
        if cmd == 'exit': break
        elif cmd == 'dev':
            mode = "DEV"
            calc.val = int(calc.val)
            calc.val &= calc.mask
            continue
        elif cmd == 'std': mode = "STD"; continue
        elif cmd == 'c': calc.clear(); continue
            
        if cmd in ['qword', 'dword', 'word', 'byte']:
            print(calc.set_bit_mode(cmd))
            continue

        if cmd in ['hex', 'dec', 'bin', 'oct']:
            print(calc.set_base(cmd))
            continue
        
        if cmd == 'ms': print(calc.memory_save()); continue
        if cmd == 'mr': print(calc.memory_recall()); continue
        if cmd == 'mc': print(calc.memory_clear_all()); continue
        if cmd == 'm+': 
            try:
                val = float(input("Value to M+: ") or calc.val)
                print(calc.memory_add(val))
            except: print("Error")
            continue

        try:
            if mode == "STD":
                if cmd == 'v': print(f"Result: {calc.sqrt()}")
                elif cmd == '1/x': print(f"Result: {calc.inverse()}")
                elif cmd == 'inv': print(f"Result: {calc.negate()}")
                elif cmd in ['+', '-', '*', '/', '%', '^']:
                    b_str = input("Enter number b: ")
                    b = float(b_str)
                    if cmd == '+': print(f"Result: {calc.add(b)}")
                    elif cmd == '-': print(f"Result: {calc.subtract(b)}")
                    elif cmd == '*': print(f"Result: {calc.multiply(b)}")
                    elif cmd == '/': print(f"Result: {calc.divide(b)}")
                    elif cmd == '%': print(f"Result: {calc.percent(b)}")
                    elif cmd == '^': print(f"Result: {calc.power(b)}")
                else:
                    try: calc.val = float(cmd)
                    except: print("Unknown command")

            elif mode == "DEV":
                if cmd == '~':
                    print(f"Result: {calc.not_developer(None)}")
                elif cmd in ['+', '-', '*', '/', '&', '|', '^', '<<', '>>']:
                    b_raw = input(f"Enter b (current base={calc.base}): ")

                    if cmd == '+': print(f"Result: {calc.add_developer(None, b_raw)}")
                    elif cmd == '-': print(f"Result: {calc.subtract_developer(None, b_raw)}")
                    elif cmd == '*': print(f"Result: {calc.multiply_developer(None, b_raw)}")
                    elif cmd == '/': print(f"Result: {calc.divide_developer(None, b_raw)}")
                    elif cmd == '&': print(f"Result: {calc.and_developer(None, b_raw)}")
                    elif cmd == '|': print(f"Result: {calc.or_developer(None, b_raw)}")
                    elif cmd == '^': print(f"Result: {calc.xor_developer(None, b_raw)}")
                    elif cmd == '<<': print(f"Result: {calc.lsh_developer(None, b_raw)}")
                    elif cmd == '>>': print(f"Result: {calc.rsh_developer(None, b_raw)}")
                else:
                    try:
                        new_val = calc._parse_dev_input(cmd)
                        calc.val = new_val & calc.mask
                    except:
                        print("Unknown command")

        except Exception as e:
            print(f"Error: {e}")

if __name__ == "__main__":
    cli_interface()