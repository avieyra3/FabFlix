import sys

def log_processing(text):
    average_tj: int = 0
    average_ts: int = 0
    log_lines : int = 0

    for i in range(1, len(sys.argv)):

        with open(sys.argv[i], "r") as f:

            # read each line and parse, the format is constant so we can assume
            # that the below loop will always parse correctly
            # example line: TJ 123456 TS 987654
            #               0    1     2   3
            lines = f.readlines()
            log_lines += len(lines)
            #get sum for tj & ts
            for line in lines:
                line_list = line.split()
                average_tj += int(line_list[1])
                average_ts += int(line_list[3])
            f.close()

    # calculate average
    average_tj = average_tj / log_lines
    average_ts = average_ts / log_lines
    print("average tj = " + str(average_tj))
    print("average ts = " + str(average_ts))

def main():
    log_processing(sys.argv)

if __name__ == "__main__":
    main()