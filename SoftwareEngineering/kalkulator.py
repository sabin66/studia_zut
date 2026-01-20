import math
import os

class Calc:
    def __init__(self):
        self.mem = []
        self.val: int = 0

    def _update_state(self, result):
        self.val = result
        return result
    
    def _get_a(self, a):
        if a is None:
            return self.val
        if isinstance(a, str):
            # Jeśli podano string binarny w zwykłym trybie, konwertujemy
            try:
                return int(a, 2)
            except ValueError:
                return float(a)
        else:
            return a

    # --- ORYGINALNE FUNKCJE DEVELOPER (BITOWE/BINARNE) ---
    def multiply_developer(self, a, b):
        a, b = self.val if a is None else int(a, 2), int(b, 2)
        sol = a * b
        self.val = sol
        return bin(sol & 0xFF).replace('0b', '')
    
    def add_developer(self, a, b):
        a, b = self.val if a is None else int(a, 2), int(b, 2)
        sol = a + b
        self.val = sol
        return bin(sol & 0xFF).replace('0b', '')
    
    def subtract_developer(self, a, b):
        a, b = self.val if a is None else int(a, 2), int(b, 2)
        sol = a - b
        self.val = sol
        return (bin(sol & 0xFF).replace('0b', ''))
    
    def divide_developer(self, a, b):
        a, b = self.val if a is None else int(a, 2), int(b, 2)
        if b == 0: return "ZeroDivisionError"
        sol = int(a // b) 
        self.val = sol
        return bin(sol & 0xFF).replace('0b', '')

    def and_developer(self, a, b):
        a, b = self.val if a is None else int(a, 2), int(b, 2)
        sol = a & b
        self.val = sol
        return bin(sol & 0xFF).replace('0b', '')

    def or_developer(self, a, b):
        a, b = self.val if a is None else int(a, 2), int(b, 2)
        sol = a | b
        self.val = sol
        return bin(sol & 0xFF).replace('0b', '')

    def xor_developer(self, a, b):
        a, b = self.val if a is None else int(a, 2), int(b, 2)
        sol = a ^ b
        self.val = sol
        return bin(sol & 0xFF).replace('0b', '')
    
    def not_developer(self, a):
        a = self.val if a is None else int(a, 2)
        sol = ~a
        self.val = sol
        return bin(sol & 0xFF).replace('0b', '')

    def lsh_developer(self, a, b):
        a = self.val if a is None else int(a, 2)
        shift = int(str(b), 2) if isinstance(b, str) else int(b)
        sol = a << shift
        self.val = sol
        return bin(sol & 0xFF).replace('0b', '')

    def rsh_developer(self, a, b):
        a = self.val if a is None else int(a, 2)
        shift = int(str(b), 2) if isinstance(b, str) else int(b)
        sol = a >> shift
        self.val = sol
        return bin(sol & 0xFF).replace('0b', '')

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
        """Reszta z dzielenia"""
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
        if not self.mem:
            return 0
        self.val = self.mem[-1]
        return self.val

    def memory_clear_all(self):
        self.mem = []
        return "Memory Cleared"
    
    def memory_add(self, val=None):
        v = val if val is not None else self.val
        if not self.mem:
            self.mem.append(0)
        self.mem[-1] += v
        return f"Mem: {self.mem[-1]}"

    def memory_sub(self, val=None):
        v = val if val is not None else self.val
        if not self.mem:
            self.mem.append(0)
        self.mem[-1] -= v
        return f"Mem: {self.mem[-1]}"

def cli_interface():
    calc = Calc()
    mode = "STD"
    
    while True:
        print("\n" + "="*40)
        print(f"WINDOWS 10 CALCULATOR (Mode: {mode})")
        if mode == "DEV":
            print(f"Display (Val): {bin(calc.val).replace('0b','')}")
        else:
            print(f"Display (Val): {calc.val}")
        print(f"Memory: {calc.mem}")
        print("-" * 40)
        
        if mode == "STD":
            print("Commands: +, -, *, /, %, ^ (pow), v (sqrt), 1/x, inv (+/-)")
            print("Memory: ms, mr, mc, m+, m-")
            print("Other: c (clear), dev (switch mode), exit")
        else:
            print("Binary Mode (Inputs should be bin strings or int)")
            print("Commands: +, -, *, /, &, |, ^ (xor), ~ (not), <<, >>")
            print("Other: c (clear), std (switch mode), exit")
        
        cmd = input(">>> ").strip().lower()
        
        if cmd == 'exit':
            break
        elif cmd == 'dev':
            mode = "DEV"
            calc.val = int(calc.val)
            continue
        elif cmd == 'std':
            mode = "STD"
            continue
        elif cmd == 'c':
            calc.clear()
            continue
        
        # Memory commands
        if cmd == 'ms': print(calc.memory_save()); continue
        if cmd == 'mr': print(calc.memory_recall()); continue
        if cmd == 'mc': print(calc.memory_clear_all()); continue
        if cmd == 'm+': 
            try:
                val = float(input("Value to M+ (enter for current): ") or calc.val)
                print(calc.memory_add(val))
            except: print("Error")
            continue

        try:
            if mode == "STD":
                if cmd == 'v':
                    print(f"Result: {calc.sqrt()}")
                elif cmd == '1/x':
                    print(f"Result: {calc.inverse()}")
                elif cmd == 'inv':
                    print(f"Result: {calc.negate()}")
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
                    # Try to enter a number directly to set state
                    try:
                        calc.val = float(cmd)
                    except:
                        print("Unknown command")

            elif mode == "DEV":
                # W trybie DEV oczekujemy inputu binarnego dla a i b jeśli chcemy użyć funkcji _developer
                # Dla uproszczenia CLI, używamy aktualnego self.val jako 'a', a użytkownik podaje 'b'
                if cmd == '~':
                    # NOT działa na self.val, musimy zamienić na bin string dla funkcji
                    val_bin = bin(int(calc.val)).replace('0b', '')
                    print(f"Result (bin): {calc.not_developer(val_bin)}")
                elif cmd in ['+', '-', '*', '/', '&', '|', '^', '<<', '>>']:
                    b_raw = input("Enter b (binary string ex. 101): ")
                    # Konwersja aktualnego stanu na bin string dla funkcji _developer
                    a_bin = bin(int(calc.val)).replace('0b', '')
                    
                    if cmd == '+': print(f"Result (bin): {calc.add_developer(a_bin, b_raw)}")
                    elif cmd == '-': print(f"Result (bin): {calc.subtract_developer(a_bin, b_raw)}")
                    elif cmd == '*': print(f"Result (bin): {calc.multiply_developer(a_bin, b_raw)}")
                    elif cmd == '/': print(f"Result (bin): {calc.divide_developer(a_bin, b_raw)}")
                    elif cmd == '&': print(f"Result (bin): {calc.and_developer(a_bin, b_raw)}")
                    elif cmd == '|': print(f"Result (bin): {calc.or_developer(a_bin, b_raw)}")
                    elif cmd == '^': print(f"Result (bin): {calc.xor_developer(a_bin, b_raw)}")
                    elif cmd == '<<': print(f"Result (bin): {calc.lsh_developer(a_bin, b_raw)}")
                    elif cmd == '>>': print(f"Result (bin): {calc.rsh_developer(a_bin, b_raw)}")
                else:
                     try:
                        # Pozwala wpisać liczbę (binarnie), żeby ustawić stan
                        calc.val = int(cmd, 2)
                     except:
                        print("Unknown command or invalid binary")

        except Exception as e:
            print(f"Error: {e}")

if __name__ == "__main__":
    print("--- Starting CLI ---")
    cli_interface()