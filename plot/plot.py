import folium


def map_inst_ids_to_location():
    pass


def define_map():
    middle = [60.793142, 3.601824]
    m = folium.Map(location=middle,
                   zoom_start=8,
                   zoom_control=False)
    folium.TileLayer('cartodbpositron').add_to(m)
    return m


def add_markers(m, installations):
    for installation in installations:
        location = get_location(installation)
        folium.CircleMarker(location=location,
                            radius=4,
                            color='lightblue',
                            fill_color='black',
                            fill_opacity=1,
                            fill=True).add_to(m)


def get_location(installation):
    return 60.48, 2.32


def save_map(m, file_name):
    m.save(f'{file_name}.html')


if __name__ == '__main__':
    print('Hello')
