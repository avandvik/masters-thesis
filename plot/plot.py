import folium
import json
import os
import selenium.webdriver


local = False
solstorm_dir = '060421-205532'  # If results are in directory collected from Solstorm

root_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
resources_path = '/src/main/resources'
solution_path = root_path + '/plot/solution'
plots_path = f'{root_path}/plot/plots'
installations_path = root_path + resources_path + '/constant/installations.json'
instance_path = f'{root_path}{resources_path}/instance'


def map_inst_ids_to_location():
    with open(installations_path) as json_installations:
        installations = json.load(json_installations)
    inst_id_to_location = dict()
    for key in installations:
        installation_id = installations.get(key).get('id')
        latitude = installations.get(key).get('latitude')
        longitude = installations.get(key).get('longitude')
        inst_id_to_location.update({installation_id: (latitude, longitude)})
    return inst_id_to_location


def get_solution():
    files = os.listdir(solution_path)
    if len(files) != 1:
        raise FileNotFoundError('Make sure that there is only one file in solution directory!')

    with open(f'{solution_path}/{files[0]}') as json_solution:
        return json.load(json_solution)


def get_instance():
    instance_name = get_instance_name()
    with open(f'{instance_path}/{instance_name}') as json_instance:
        return json.load(json_instance)


def get_instance_name():
    return solution.get('instance')


def loop_through_solution():
    m = define_map()
    for vessel in solution.get('voyages').keys():
        order_sequence = solution.get('voyages').get(vessel).get('sequence')
        if len(order_sequence) == 0:
            continue
        installation_sequence = [0]
        for order in order_sequence:
            order_info = instance.get('orders').get(str(order))
            installation_id = order_info.get('installation')
            if installation_id not in installation_sequence:
                installation_sequence.append(installation_id)
        add_markers(m, installation_sequence)
    save_map_png(m, 'test')


def define_map():
    middle = [60.793142, 3.601824]
    m = folium.Map(location=middle,
                   zoom_start=8,
                   zoom_control=False)
    folium.TileLayer('cartodbpositron').add_to(m)
    return m


def add_markers(m, installations):
    points = []
    for installation in installations:
        location = get_location(installation)
        points.append(location)
        folium.CircleMarker(location=location,
                            radius=4,
                            color='darkblue',
                            fill_color='white',
                            fill_opacity=1,
                            fill=True).add_to(m)
    points.append(get_location(0))
    folium.PolyLine(points, color="green", weight=2.5, opacity=1).add_to(m)


def get_location(installation):
    return inst_ids_to_location.get(installation)


def save_map_html(m, file_name):
    html_name = f'{plots_path}/{file_name}.html'
    m.save(html_name)
    return html_name


def save_map_png(m, file_name):
    html_name = save_map_html(m, file_name)
    driver = selenium.webdriver.PhantomJS()
    driver.set_window_size(1000, 700)  # choose a resolution
    driver.get(html_name)
    driver.save_screenshot(f'{plots_path}/{file_name}.png')
    os.remove(f'{plots_path}/{file_name}.html')


inst_ids_to_location = map_inst_ids_to_location()
solution = get_solution()
instance = get_instance()
loop_through_solution()
