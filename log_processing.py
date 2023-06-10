with open("timelog", "r") as f:

    average_tj: int = 0
    average_ts: int = 0

    # read each line and parse, the format is constant so we can assume
    # that the below loop will always parse correctly
    # example line: TJ 123456 TS 987654
    #               0    1     2   3
    lines = f.readlines()
    # get sum for tj & ts
    for line in lines:
        line_list = line.split()
        average_tj += int(line_list[1])
        average_ts += int(line_list[3])

    # calculate average
    average_tj = average_tj / len(lines)
    average_ts = average_ts / len(lines)
    print("average tj = " + str(average_tj))
    print("average ts = " + str(average_ts))
    f.close()