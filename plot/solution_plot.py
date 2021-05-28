import folium
import json
import os
import re
import selenium.webdriver


"""
TO INSTALL:
    - Create a virtual environment (e.g. python3 -m venv ./venv)
    - Source virtual environment (e.g. source ./venv/bin/activate) 
    - Install requirements.txt (e.g. pip install -r requirements.txt)
    - Deactivate the virtual environment (e.g. deactivate)
    - Install phantomjs (e.g. brew install phantomjs)

TO RUN:
    - Make sure you have the directories plot/plots and plot/solution
    - Put the solution json file you want to plot in the plot/solution directory
    - Make sure that there is only one file in the plot/solution directory
    - Run this file, output gets saved in plot/plots
"""

root_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
resources_path = '/src/main/resources'
solution_path = root_path + '/plot/solution'
plots_path = f'{root_path}/plot/solutions'
installations_path = root_path + resources_path + '/constant/installations.json'
instance_path = f'{root_path}{resources_path}/instances'
vessel_to_color = {0: "green", 1: "blue", 2: "yellow", 3: "black", 4: "white", 5: "pink"}


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


def save_solution_plot():
    m_1 = define_map()
    m_2 = define_map()
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
        add_markers(m_1, installation_sequence)
        add_markers(m_2, installation_sequence)
        add_lines(m_2, installation_sequence, vessel)
    instance_name = get_instance_name().split('.')[0]
    save_map_png(m_1, f'{instance_name}_markers')
    save_map_png(m_2, f'{instance_name}_markers_lines')


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


def add_lines(m, installations, vessel):
    points = []
    for installation in installations:
        location = get_location(installation)
        points.append(location)
    points.append(get_location(0))
    v_idx = int(re.split('_', vessel)[1])
    color = vessel_to_color.get(v_idx)
    folium.PolyLine(points, color=color, weight=2.5, opacity=1).add_to(m)


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
save_solution_plot()
